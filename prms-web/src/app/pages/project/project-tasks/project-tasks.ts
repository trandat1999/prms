import { CommonModule, DatePipe } from '@angular/common';
import { Component, DestroyRef, Input, OnChanges, OnInit, SimpleChanges, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { NzButtonComponent } from 'ng-zorro-antd/button';
import { NzIconDirective } from 'ng-zorro-antd/icon';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { NzSpinComponent } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagComponent } from 'ng-zorro-antd/tag';
import { InputCommon } from '../../../shared/input/input';
import { ApiResponse } from '../../../shared/utils/api-response';
import {
  applyServerFieldErrorsToFormGroup,
  clearServerErrorsOnFormGroup,
  parseServerFieldErrorMap,
  SERVER_FORM_ERROR_KEY,
} from '../../../shared/utils/form-server-errors';
import {
  CHECKLIST_TITLE_REQUIRED_ERROR_KEY,
  isInvalidAndTouched,
  markFormControlsTouched,
  trimRequiredValidator,
} from '../../../shared/utils/form-validation';
import { Page } from '../models/page.model';
import { Task, TaskChecklistItem, TaskLog, TaskWritePayload } from '../models/task.model';
import { TaskSearchRequest } from '../models/task-search.request';
import { TaskStatus } from '../models/task.types';
import { TaskService } from '../services/task.service';
import { StoreService } from '../../../core/services/store-service';
import { ProjectService } from '../services/project.service';

type TaskChecklistRow = { title: string; estimatedHours: number | null; checked: boolean };

@Component({
  selector: 'app-project-tasks',
  imports: [
    CommonModule,
    DatePipe,
    FormsModule,
    ReactiveFormsModule,
    NzTableModule,
    NzTagComponent,
    NzButtonComponent,
    NzIconDirective,
    NzModalModule,
    NzSpinComponent,
    InputCommon,
    TranslatePipe,
  ],
  templateUrl: './project-tasks.html',
  styleUrls: ['./project-tasks.scss'],
})
export class ProjectTasks implements OnChanges, OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private syncingEstimatedHours = false;
  private estimatedHoursDerivedFromChecklist = false;

  /** projectId từ URL `/project/:projectId/tasks` */
  effectiveProjectId = '';

  /** Tên dự án (optional) từ `navigate(..., { state: { projectName } })` */
  displayProjectName: string | null = null;

  @Input() projectId: string | null = null;
  @Input() projectName: string | null = null;

  keyword = '';
  status: TaskStatus | null = null;
  type: string | null = null;

  rows: Task[] = [];
  page: Page<Task> | null = null;
  pageIndex = 1;
  pageSize = 20;

  /** Modal thêm / sửa */
  formVisible = false;
  formMode: 'create' | 'edit' = 'create';
  editingId: string | null = null;
  submitting = false;
  loading = false;
  checklistRows: TaskChecklistRow[] = [];
  readonly taskModalForm = new FormGroup({
    code: new FormControl<string>('', [Validators.required, trimRequiredValidator]),
    name: new FormControl<string>('', [Validators.required, trimRequiredValidator]),
    shortDescription: new FormControl<string>(''),
    description: new FormControl<string>(''),
    status: new FormControl<TaskStatus | null>('TODO', [Validators.required]),
    priority: new FormControl<'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT' | null>('MEDIUM', [Validators.required]),
    estimatedHours: new FormControl<number | null>(null, [Validators.min(0)]),
    actualHours: new FormControl<number | null>(null, [Validators.min(0)]),
    assignedId: new FormControl<string | null>(null),
    label: new FormControl<string>(''),
    type: new FormControl<string | null>(null),
    predecessorIds: new FormControl<string[]>([]),
    /** Chỉ để map lỗi BE field `checklists` (không bind UI). */
    checklists: new FormControl<unknown>(null, [this.checklistValidator()]),
  });
  readonly assignModalForm = new FormGroup({
    assignedId: new FormControl<string | null>(null, [Validators.required]),
  });

  /** Modal xem + log */
  viewVisible = false;
  viewLoading = false;
  viewDetail: Task | null = null;
  logsLoading = false;
  logs: TaskLog[] = [];

  /** Modal assign */
  assignVisible = false;
  assignSubmitting = false;
  assignTaskId: string | null = null;

  readonly userAutocompleteUrl = '/api/v1/autocomplete/users';
  readonly labelAppParamUrl = '/api/v1/app-params/page';
  readonly labelAppParamFilters = { paramGroup: 'MODULE_TASK', paramType: 'TASK_LABLE' };
  readonly typeAppParamUrl = '/api/v1/app-params/page';
  readonly typeAppParamFilters = { paramGroup: 'MODULE_TASK', paramType: 'TASK_TYPE' };

  /** Ứng viên cho multiselect tiên quyết (cùng dự án) */
  predecessorTaskCandidates: Task[] = [];

  /** Chỉ PM (PROJECT_MANAGER) mới được tạo/sửa/xoá/assign */
  isProjectManager = false;
  private currentUserId: string | null = null;

  readonly statusOptions: { label: string; value: TaskStatus }[] = [
    { label: 'Todo', value: 'TODO' },
    { label: 'In Progress', value: 'IN_PROGRESS' },
    { label: 'Review', value: 'REVIEW' },
    { label: 'Testing', value: 'TESTING' },
    { label: 'Done', value: 'DONE' },
  ];

  readonly priorityOptions = [
    { label: 'LOW', value: 'LOW' },
    { label: 'MEDIUM', value: 'MEDIUM' },
    { label: 'HIGH', value: 'HIGH' },
    { label: 'URGENT', value: 'URGENT' },
  ];

  constructor(
    private service: TaskService,
    private translate: TranslateService,
    private notification: NzNotificationService,
    private modal: NzModalService,
    private store: StoreService,
    private projectService: ProjectService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['projectName']) {
      const name = this.projectName?.trim();
      this.displayProjectName = name ? name : null;
    }
    if (changes['projectId'] && this.projectId) {
      this.activateProject(this.projectId, this.projectName, true);
    }
  }

  ngOnInit(): void {
    this.taskModalForm.controls.estimatedHours.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        if (!this.syncingEstimatedHours) {
          this.estimatedHoursDerivedFromChecklist = false;
        }
      });

    this.store
      .getCurrentUser()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((u) => {
        this.currentUserId = u?.id ?? null;
        this.resolveProjectRole();
      });

    if (this.projectId) {
      this.activateProject(this.projectId, this.projectName);
      return;
    }

    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((pm) => {
      const id = pm.get('projectId');
      if (!id) {
        this.notification.warning(
          this.translate.instant('common.error'),
          this.translate.instant('task.messages.missingId')
        );
        return;
      }
      const state =
        typeof history !== 'undefined' ? (history.state as { projectName?: string } | null) : null;
      this.activateProject(id, state?.projectName, true);
    });
  }

  get formTitle(): string {
    return this.formMode === 'create'
      ? this.translate.instant('task.form.createTitle')
      : this.translate.instant('task.form.editTitle');
  }

  onSearch(): void {
    this.pageIndex = 1;
    this.fetch();
  }

  onPageIndexChange(i: number): void {
    this.pageIndex = i;
    this.fetch();
  }

  onPageSizeChange(s: number): void {
    this.pageSize = s;
    this.pageIndex = 1;
    this.fetch();
  }

  onCreate(): void {
    if (!this.isProjectManager) return;
    this.formMode = 'create';
    this.editingId = null;
    this.loading = false;
    this.resetTaskModalForm();
    this.loadPredecessorCandidates();
    clearServerErrorsOnFormGroup(this.taskModalForm);
    this.formVisible = true;
  }

  onEdit(row: Task): void {
    if (!this.isProjectManager) return;
    const id = row?.id;
    if (!id) {
      this.notification.warning(this.translate.instant('common.error'), this.translate.instant('task.messages.missingId'));
      return;
    }
    this.formMode = 'edit';
    this.editingId = id;
    this.formVisible = true;
    this.loading = true;
    this.resetTaskModalForm();
    clearServerErrorsOnFormGroup(this.taskModalForm);
    this.loadPredecessorCandidates();
    this.service.getById(id).subscribe({
      next: ({ raw, task }) => {
        this.loading = false;
        if (raw?.code === 200 && task) {
          this.applyToForm(task);
        } else {
          this.notifyFromResponse(raw, this.translate.instant('task.messages.loadFailed'));
          this.formVisible = false;
        }
      },
      error: () => {
        this.loading = false;
        this.formVisible = false;
      },
    });
  }

  onView(row: Task): void {
    const id = row?.id;
    if (!id) return;
    this.viewVisible = true;
    this.viewDetail = null;
    this.logs = [];
    this.viewLoading = true;
    this.service.getById(id).subscribe({
      next: ({ raw, task }) => {
        this.viewLoading = false;
        if (raw?.code === 200 && task) {
          this.viewDetail = task;
          this.fetchLogs(id);
        } else {
          this.notifyFromResponse(raw, this.translate.instant('task.messages.loadFailed'));
          this.viewVisible = false;
        }
      },
      error: () => {
        this.viewLoading = false;
        this.viewVisible = false;
      },
    });
  }

  onDelete(row: Task): void {
    if (!this.isProjectManager) return;
    const id = row?.id;
    if (!id) {
      this.notification.warning(this.translate.instant('common.error'), this.translate.instant('task.messages.missingId'));
      return;
    }
    const label = row.name?.trim() || row.code || id;
    this.modal.confirm({
      nzTitle: this.translate.instant('task.messages.confirmDeleteTitle'),
      nzContent: this.translate.instant('task.messages.confirmDeleteContent').replace('{{label}}', label),
      nzOkText: this.translate.instant('common.button.delete'),
      nzOkDanger: true,
      nzCancelText: this.translate.instant('common.button.cancel'),
      nzOnOk: () =>
        new Promise<void>((resolve, reject) => {
          this.service.delete(id).subscribe({
            next: ({ raw }) => {
              if (raw?.code === 200) {
                this.notification.success(this.translate.instant('common.button.done'), raw?.message ?? '');
                this.fetch();
                resolve();
              } else {
                this.notifyFromResponse(raw, this.translate.instant('task.messages.deleteFailed'));
                reject();
              }
            },
            error: () => reject(),
          });
        }),
    });
  }

  onOpenAssign(row: Task): void {
    if (!this.isProjectManager) return;
    const id = row?.id;
    if (!id) return;
    this.assignTaskId = id;
    this.assignModalForm.reset({ assignedId: row.assignedId ?? null });
    clearServerErrorsOnFormGroup(this.assignModalForm);
    this.assignVisible = true;
  }

  closeAssign(): void {
    this.assignVisible = false;
    this.assignSubmitting = false;
    this.assignTaskId = null;
    clearServerErrorsOnFormGroup(this.assignModalForm);
  }

  saveAssign(): void {
    const id = this.assignTaskId;
    const assignedId = this.assignModalForm.get('assignedId')?.value;
    clearServerErrorsOnFormGroup(this.assignModalForm);
    if (!id) {
      return;
    }
    if (this.assignModalForm.invalid) {
      markFormControlsTouched(this.assignModalForm);
      return;
    }
    this.assignSubmitting = true;
    this.service.assign(id, { assignedId: assignedId as string }).subscribe({
      next: ({ raw }) => {
        this.assignSubmitting = false;
        if (raw?.code === 200) {
          this.notification.success(this.translate.instant('common.button.done'), raw?.message ?? '');
          this.closeAssign();
          this.fetch();
          return;
        }
        this.handleWriteError(raw, 'assign');
      },
      error: () => (this.assignSubmitting = false),
    });
  }

  closeModal(): void {
    this.formVisible = false;
    clearServerErrorsOnFormGroup(this.taskModalForm);
  }

  closeView(): void {
    this.viewVisible = false;
    this.viewDetail = null;
    this.logs = [];
  }

  save(): void {
    if (!this.isProjectManager) return;
    if (this.formMode === 'edit' && this.loading) return;
    clearServerErrorsOnFormGroup(this.taskModalForm);
    this.syncEstimatedFromChecklist();
    this.taskModalForm.controls.checklists.updateValueAndValidity();
    if (this.taskModalForm.invalid) {
      markFormControlsTouched(this.taskModalForm);
      return;
    }

    const f = this.taskModalForm.getRawValue();
    const payload: TaskWritePayload = {
      code: f.code!.trim(),
      name: f.name!.trim(),
      shortDescription: f.shortDescription?.trim() ? f.shortDescription.trim() : null,
      description: f.description?.trim() ? f.description.trim() : null,
      projectId: this.effectiveProjectId,
      status: f.status,
      priority: f.priority,
      type: f.type?.trim() ? f.type.trim() : null,
      estimatedHours: f.estimatedHours,
      actualHours: f.actualHours,
      assignedId: f.assignedId,
      label: f.label?.trim() ? f.label.trim() : null,
      checklists: this.buildChecklistPayload(),
      predecessorTaskIds: [...(f.predecessorIds ?? [])],
    };

    if (this.formMode === 'create') {
      this.submitting = true;
      this.service.create(payload).subscribe({
        next: ({ raw }) => {
          this.submitting = false;
          if (raw?.code === 201) {
            this.notification.success(this.translate.instant('common.button.done'), raw?.message ?? '');
            clearServerErrorsOnFormGroup(this.taskModalForm);
            this.formVisible = false;
            this.fetch();
            return;
          }
          this.handleWriteError(raw);
        },
        error: () => (this.submitting = false),
      });
      return;
    }

    const id = this.editingId;
    if (!id) {
      this.notification.warning(this.translate.instant('common.error'), this.translate.instant('task.messages.missingId'));
      return;
    }
    this.submitting = true;
    this.service.update(id, payload).subscribe({
      next: ({ raw }) => {
        this.submitting = false;
        if (raw?.code === 200) {
          this.notification.success(this.translate.instant('common.button.done'), raw?.message ?? '');
          clearServerErrorsOnFormGroup(this.taskModalForm);
          this.formVisible = false;
          this.fetch();
          return;
        }
        this.handleWriteError(raw);
      },
      error: () => (this.submitting = false),
    });
  }

  statusLabel(s?: TaskStatus | null): string {
    return this.statusOptions.find((x) => x.value === s)?.label ?? (s ?? '—');
  }

  private fetch(): void {
    if (!this.effectiveProjectId) {
      return;
    }
    const req: TaskSearchRequest = {
      keyword: this.keyword?.trim() || null,
      status: this.status,
      type: this.type?.trim() ? this.type.trim() : null,
      projectId: this.effectiveProjectId,
      pageIndex: Math.max(0, (this.pageIndex ?? 1) - 1),
      pageSize: this.pageSize,
      voided: false,
    };
    this.service.getPage(req).subscribe(({ page }) => {
      this.page = page;
      this.rows = page?.content ?? [];
      this.pageSize = page?.size ?? this.pageSize;
      this.pageIndex = (page?.number ?? req.pageIndex ?? 0) + 1;
    });
  }

  private activateProject(id: string, projectName?: string | null, fetchAfterChange = false): void {
    const name = projectName?.trim();
    if (name) {
      this.displayProjectName = name;
    }
    const changed = id !== this.effectiveProjectId;
    if (changed) {
      this.effectiveProjectId = id;
      this.pageIndex = 1;
      this.loadPredecessorCandidates();
      this.resolveProjectRole();
    }
    if (changed || fetchAfterChange) {
      this.fetch();
    }
  }

  private resolveProjectRole(): void {
    if (!this.effectiveProjectId || !this.currentUserId) {
      this.isProjectManager = false;
      return;
    }
    this.projectService.getById(this.effectiveProjectId).subscribe({
      next: ({ raw, project }) => {
        if (raw?.code === 200 && project?.managerId) {
          this.isProjectManager = project.managerId === this.currentUserId;
          return;
        }
        this.isProjectManager = false;
      },
      error: () => (this.isProjectManager = false),
    });
  }

  private fetchLogs(taskId: string): void {
    this.logsLoading = true;
    this.service.getLogs(taskId).subscribe({
      next: ({ raw, logs }) => {
        this.logsLoading = false;
        if (raw?.code === 200) {
          this.logs = Array.isArray(logs) ? logs : [];
        }
      },
      error: () => (this.logsLoading = false),
    });
  }

  get checklistHoursSum(): number {
    return this.buildChecklistPayload().reduce((sum, item) => sum + (Number(item.estimatedHours) || 0), 0);
  }

  get taskChecklistServerError(): string | null {
    const ctrl = this.taskModalForm.controls.checklists;
    if (!isInvalidAndTouched(ctrl)) {
      return null;
    }
    const err = ctrl.errors;
    const msg = err?.[SERVER_FORM_ERROR_KEY];
    return typeof msg === 'string' && msg.trim() ? msg : null;
  }

  get taskChecklistValidationError(): string | null {
    const ctrl = this.taskModalForm.controls.checklists;
    if (!isInvalidAndTouched(ctrl)) {
      return null;
    }
    return ctrl.errors?.[CHECKLIST_TITLE_REQUIRED_ERROR_KEY]
      ? this.translate.instant('task.checklist.titleRequired')
      : this.taskChecklistServerError;
  }

  get predecessorSelectItems(): { id: string; label: string }[] {
    return (this.predecessorTaskCandidates ?? [])
      .filter((t) => t.id && t.id !== this.editingId)
      .map((t) => ({
        id: t.id as string,
        label: `${t.code ?? ''} — ${t.name ?? ''}`.trim(),
      }));
  }

  addChecklistRow(): void {
    this.checklistRows = [...(this.checklistRows ?? []), { title: '', estimatedHours: null, checked: false }];
    this.taskModalForm.controls.checklists.updateValueAndValidity();
  }

  removeChecklistRow(index: number): void {
    const next = [...(this.checklistRows ?? [])];
    next.splice(index, 1);
    this.checklistRows = next;
    this.syncEstimatedFromChecklist();
    this.taskModalForm.controls.checklists.updateValueAndValidity();
  }

  onChecklistCheckedChange(index: number, checked: boolean): void {
    this.updateChecklistRow(index, { checked });
  }

  onChecklistTitleChange(index: number, title: string): void {
    this.updateChecklistRow(index, { title });
  }

  onChecklistHoursChange(index: number, estimatedHours: string | number | null): void {
    const normalized = estimatedHours === '' || estimatedHours == null ? null : Number(estimatedHours);
    this.updateChecklistRow(index, {
      estimatedHours: normalized != null && !Number.isNaN(normalized) ? normalized : null,
    });
  }

  isChecklistTitleInvalid(index: number): boolean {
    return isInvalidAndTouched(this.taskModalForm.controls.checklists) && this.isChecklistRowTitleRequired(index);
  }

  private loadPredecessorCandidates(): void {
    if (!this.effectiveProjectId) {
      this.predecessorTaskCandidates = [];
      return;
    }
    this.service
      .getPage({
        projectId: this.effectiveProjectId,
        pageIndex: 0,
        pageSize: 500,
        voided: false,
      })
      .subscribe(({ page }) => {
        this.predecessorTaskCandidates = page?.content ?? [];
      });
  }

  private syncEstimatedFromChecklist(): void {
    const s = this.checklistHoursSum;
    if (s > 0) {
      this.setEstimatedHoursFromChecklist(s);
      return;
    }
    if (this.estimatedHoursDerivedFromChecklist) {
      this.setEstimatedHoursFromChecklist(null);
    }
  }

  private buildChecklistPayload(): TaskChecklistItem[] {
    return (this.checklistRows ?? [])
      .map((row, i) => ({
        title: row.title?.trim() ?? '',
        checked: !!row.checked,
        sortOrder: i,
        estimatedHours: row.estimatedHours != null ? Number(row.estimatedHours) : null,
      }))
      .filter((x) => x.title.length > 0);
  }

  private checklistValidator(): (control: AbstractControl) => ValidationErrors | null {
    return () => {
      return (this.checklistRows ?? []).some((_, index) => this.isChecklistRowTitleRequired(index))
        ? { [CHECKLIST_TITLE_REQUIRED_ERROR_KEY]: true }
        : null;
    };
  }

  private isChecklistRowTitleRequired(index: number): boolean {
    const row = this.checklistRows?.[index];
    if (!row) {
      return false;
    }
    const hasTitle = !!row.title?.trim();
    const hasHours = row.estimatedHours != null && !Number.isNaN(Number(row.estimatedHours));
    return !hasTitle && (hasHours || row.checked);
  }

  private resetTaskModalForm(): void {
    this.checklistRows = [];
    this.estimatedHoursDerivedFromChecklist = false;
    this.taskModalForm.reset({
      code: '',
      name: '',
      shortDescription: '',
      description: '',
      status: 'TODO',
      priority: 'MEDIUM',
      type: null,
      estimatedHours: null,
      actualHours: null,
      assignedId: null,
      label: '',
      predecessorIds: [],
      checklists: null,
    });
    this.taskModalForm.controls.checklists.updateValueAndValidity();
  }

  private applyToForm(t: Task): void {
    const predIds =
      (t.predecessorTaskIds?.length ? t.predecessorTaskIds : t.predecessors?.map((p) => p.id).filter(Boolean)) ?? [];
    this.taskModalForm.patchValue({
      code: t.code ?? '',
      name: t.name ?? '',
      shortDescription: (t.shortDescription ?? '') as string,
      description: (t.description ?? '') as string,
      status: (t.status ?? 'TODO') as TaskStatus,
      priority: (t.priority ?? 'MEDIUM') as 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT',
      type: (t.type ?? null) as string | null,
      estimatedHours: t.estimatedHours ?? null,
      actualHours: t.actualHours ?? null,
      assignedId: (t.assignedId ?? null) as string | null,
      label: (t.label ?? '') as string,
      predecessorIds: predIds as string[],
      checklists: null,
    });
    this.checklistRows = (t.checklists ?? []).map((c) => ({
      title: c.title ?? '',
      estimatedHours: c.estimatedHours ?? null,
      checked: !!c.checked,
    }));
    this.syncEstimatedFromChecklist();
    this.taskModalForm.controls.checklists.updateValueAndValidity();
  }

  private handleWriteError(raw: ApiResponse | undefined, target: 'task' | 'assign' = 'task'): void {
    const map = parseServerFieldErrorMap(raw);
    if (map) {
      if (target === 'assign') {
        const normalized = { ...map };
        if (!normalized['assignedId'] && normalized['assignUserId']) {
          normalized['assignedId'] = normalized['assignUserId'];
        }
        applyServerFieldErrorsToFormGroup(this.assignModalForm, normalized);
        markFormControlsTouched(this.assignModalForm);
      } else {
        const normalized = { ...map };
        if (!normalized['predecessorIds'] && normalized['predecessorTaskIds']) {
          normalized['predecessorIds'] = normalized['predecessorTaskIds'];
        }
        applyServerFieldErrorsToFormGroup(this.taskModalForm, normalized);
        markFormControlsTouched(this.taskModalForm);
      }
      return;
    }
    if (target === 'assign') {
      clearServerErrorsOnFormGroup(this.assignModalForm);
    } else {
      clearServerErrorsOnFormGroup(this.taskModalForm);
    }
    this.notification.warning(this.translate.instant('common.error'), raw?.message ?? '');
  }

  private notifyFromResponse(raw: ApiResponse | undefined, fallback: string): void {
    this.notification.warning(this.translate.instant('common.error'), raw?.message ?? fallback);
  }

  private updateChecklistRow(index: number, patch: Partial<TaskChecklistRow>): void {
    const next = [...(this.checklistRows ?? [])];
    const row = next[index];
    if (!row) {
      return;
    }
    next[index] = { ...row, ...patch };
    this.checklistRows = next;
    this.syncEstimatedFromChecklist();
    this.taskModalForm.controls.checklists.updateValueAndValidity();
  }

  private setEstimatedHoursFromChecklist(value: number | null): void {
    this.syncingEstimatedHours = true;
    this.taskModalForm.controls.estimatedHours.setValue(value);
    this.syncingEstimatedHours = false;
    this.estimatedHoursDerivedFromChecklist = value != null;
  }
}
