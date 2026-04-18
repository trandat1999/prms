import { CommonModule, DatePipe } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { NzButtonComponent } from 'ng-zorro-antd/button';
import { NzIconDirective } from 'ng-zorro-antd/icon';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { NzSpinComponent } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagComponent } from 'ng-zorro-antd/tag';
import { InputCommon } from '../../../../shared/input/input';
import { ApiResponse } from '../../../../shared/utils/api-response';
import { Page } from '../../../project/models/page.model';
import { Task, TaskLog, TaskWritePayload } from '../models/task.model';
import { TaskSearchRequest } from '../models/task-search.request';
import { TaskStatus } from '../models/task.types';
import { TaskService } from '../services/task.service';

type TaskFormState = {
  code: string;
  name: string;
  shortDescription: string;
  description: string;
  projectId: string | null;
  status: TaskStatus | null;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT' | null;
  estimatedHours: number | null;
  actualHours: number | null;
  assignedId: string | null;
  label: string;
  type: string | null;
};

@Component({
  selector: 'app-task-list',
  imports: [
    CommonModule,
    DatePipe,
    FormsModule,
    NzTableModule,
    NzTagComponent,
    NzButtonComponent,
    NzIconDirective,
    NzModalModule,
    NzSpinComponent,
    InputCommon,
    TranslatePipe,
  ],
  templateUrl: './task-list.html',
  styleUrls: ['./task-list.scss'],
})
export class TaskList {
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
  form: TaskFormState = this.emptyForm();

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
  assignUserId: string | null = null;
  readonly userAutocompleteUrl = '/api/v1/autocomplete/users';
  readonly projectAutocompleteUrl = '/api/v1/autocomplete/projects';
  readonly labelAppParamUrl = '/api/v1/app-params/page';
  readonly labelAppParamFilters = { paramGroup: 'MODULE_TASK', paramType: 'TASK_LABLE' };
  readonly typeAppParamUrl = '/api/v1/app-params/page';
  readonly typeAppParamFilters = { paramGroup: 'MODULE_TASK', paramType: 'TASK_TYPE' };

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
    private modal: NzModalService
  ) {}

  ngOnInit(): void {
    this.fetch();
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
    this.formMode = 'create';
    this.editingId = null;
    this.loading = false;
    this.form = this.emptyForm();
    this.formVisible = true;
  }

  onEdit(row: Task): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning(this.translate.instant('common.error'), this.translate.instant('task.messages.missingId'));
      return;
    }
    this.formMode = 'edit';
    this.editingId = id;
    this.formVisible = true;
    this.loading = true;
    this.form = this.emptyForm();
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
    const id = row?.id;
    if (!id) return;
    this.assignTaskId = id;
    this.assignUserId = row.assignedId ?? null;
    this.assignVisible = true;
  }

  closeAssign(): void {
    this.assignVisible = false;
    this.assignSubmitting = false;
    this.assignTaskId = null;
    this.assignUserId = null;
  }

  saveAssign(): void {
    const id = this.assignTaskId;
    const assignedId = this.assignUserId;
    if (!id || !assignedId) {
      this.notification.warning(this.translate.instant('task.messages.invalidTitle'), this.translate.instant('task.messages.assigneeRequired'));
      return;
    }
    this.assignSubmitting = true;
    this.service.assign(id, { assignedId }).subscribe({
      next: ({ raw }) => {
        this.assignSubmitting = false;
        if (raw?.code === 200) {
          this.notification.success(this.translate.instant('common.button.done'), raw?.message ?? '');
          this.closeAssign();
          this.fetch();
          return;
        }
        this.handleWriteError(raw);
      },
      error: () => (this.assignSubmitting = false),
    });
  }

  onStatusQuickChange(row: Task, status: TaskStatus): void {
    const id = row?.id;
    if (!id) return;
    this.service.updateStatus(id, { status }).subscribe(({ raw }) => {
      if (raw?.code === 200) {
        this.fetch();
      }
    });
  }

  closeModal(): void {
    this.formVisible = false;
  }

  closeView(): void {
    this.viewVisible = false;
    this.viewDetail = null;
    this.logs = [];
  }

  save(): void {
    if (this.formMode === 'edit' && this.loading) return;
    if (!this.validate()) return;

    const payload: TaskWritePayload = {
      code: this.form.code.trim(),
      name: this.form.name.trim(),
      shortDescription: this.form.shortDescription?.trim() ? this.form.shortDescription.trim() : null,
      description: this.form.description?.trim() ? this.form.description.trim() : null,
      projectId: this.form.projectId,
      status: this.form.status,
      priority: this.form.priority,
      type: this.form.type?.trim() ? this.form.type.trim() : null,
      estimatedHours: this.form.estimatedHours,
      actualHours: this.form.actualHours,
      assignedId: this.form.assignedId,
      label: this.form.label?.trim() ? this.form.label.trim() : null,
    };

    if (this.formMode === 'create') {
      this.submitting = true;
      this.service.create(payload).subscribe({
        next: ({ raw }) => {
          this.submitting = false;
          if (raw?.code === 201) {
            this.notification.success(this.translate.instant('common.button.done'), raw?.message ?? '');
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
    const req: TaskSearchRequest = {
      keyword: this.keyword?.trim() || null,
      status: this.status,
      type: this.type?.trim() ? this.type.trim() : null,
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

  private emptyForm(): TaskFormState {
    return {
      code: '',
      name: '',
      shortDescription: '',
      description: '',
      projectId: null,
      status: 'TODO',
      priority: 'MEDIUM',
      type: null,
      estimatedHours: null,
      actualHours: null,
      assignedId: null,
      label: '',
    };
  }

  private applyToForm(t: Task): void {
    this.form = {
      code: t.code ?? '',
      name: t.name ?? '',
      shortDescription: (t.shortDescription ?? '') as string,
      description: (t.description ?? '') as string,
      projectId: (t.projectId ?? null) as string | null,
      status: (t.status ?? 'TODO') as TaskStatus,
      priority: (t.priority ?? 'MEDIUM') as any,
      type: (t.type ?? null) as string | null,
      estimatedHours: t.estimatedHours ?? null,
      actualHours: t.actualHours ?? null,
      assignedId: (t.assignedId ?? null) as string | null,
      label: (t.label ?? '') as string,
    };
  }

  private validate(): boolean {
    if (!this.form.code?.trim()) {
      this.notification.warning(this.translate.instant('task.messages.invalidTitle'), this.translate.instant('task.messages.codeRequired'));
      return false;
    }
    if (!this.form.name?.trim()) {
      this.notification.warning(this.translate.instant('task.messages.invalidTitle'), this.translate.instant('task.messages.nameRequired'));
      return false;
    }
    return true;
  }

  private handleWriteError(raw: ApiResponse | undefined): void {
    const errBody = raw?.body;
    if (raw?.code === 400 && errBody && typeof errBody === 'object' && !Array.isArray(errBody)) {
      const msg = Object.values(errBody as Record<string, string>).filter(Boolean).join(' ');
      this.notification.warning(this.translate.instant('task.messages.invalidTitle'), msg || raw?.message || '');
      return;
    }
    this.notification.warning(this.translate.instant('common.error'), raw?.message ?? '');
  }

  private notifyFromResponse(raw: ApiResponse | undefined, fallback: string): void {
    this.notification.warning(this.translate.instant('common.error'), raw?.message ?? fallback);
  }
}

