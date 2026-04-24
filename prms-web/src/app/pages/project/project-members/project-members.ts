import { CommonModule, DatePipe } from '@angular/common';
import { Component, DestroyRef, Input, OnChanges, OnInit, SimpleChanges, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
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
} from '../../../shared/utils/form-server-errors';
import { markFormControlsTouched, siblingDateRangeValidator } from '../../../shared/utils/form-validation';
import { Page } from '../models/page.model';
import { PROJECT_MEMBER_ROLE_OPTIONS } from '../models/project-member.const';
import { ProjectMember, ProjectMemberWritePayload } from '../models/project-member.model';
import { ProjectMemberSearchRequest } from '../models/project-member-search.request';
import { ProjectMemberRole } from '../models/project-member.types';
import { ProjectMemberService } from '../services/project-member.service';
import { AppParamService } from '../../management/app-params/services/app-param.service';
import { AppParam } from '../../management/app-params/models/app-param.model';
import { StoreService } from '../../../core/services/store-service';
import { ProjectService } from '../services/project.service';

@Component({
  selector: 'app-project-members',
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
  templateUrl: './project-members.html',
  styleUrls: ['./project-members.scss'],
})
export class ProjectMembers implements OnChanges, OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  effectiveProjectId = '';
  displayProjectName: string | null = null;
  @Input() projectId: string | null = null;
  @Input() projectName: string | null = null;
  isProjectManager = false;
  private currentUserId: string | null = null;

  keyword = '';
  roleInProject: ProjectMemberRole | null = null;
  active: boolean | null = null;

  rows: ProjectMember[] = [];
  page: Page<ProjectMember> | null = null;
  pageIndex = 1;
  pageSize = 20;

  readonly userAutocompleteUrl = '/api/v1/autocomplete/users';
  roleOptions: Array<{ label: string; value: ProjectMemberRole }> = [];
  private roleLabelMap = new Map<string, string>();
  readonly activeOptions = [
    { label: 'Active', value: true },
    { label: 'Inactive', value: false },
  ];
  readonly leadOptions = [
    { label: 'Yes', value: true },
    { label: 'No', value: false },
  ];

  formVisible = false;
  formMode: 'create' | 'edit' = 'create';
  editingId: string | null = null;
  submitting = false;
  loading = false;
  readonly modalForm = new FormGroup({
    userId: new FormControl<string | null>(null, [Validators.required]),
    roleInProject: new FormControl<ProjectMemberRole | null>('DEVELOPER', [Validators.required]),
    allocationPercent: new FormControl<number | null>(100, [Validators.min(0), Validators.max(100)]),
    isLead: new FormControl<boolean>(false),
    startDate: new FormControl<Date | null>(null),
    endDate: new FormControl<Date | null>(null, [siblingDateRangeValidator('startDate')]),
    active: new FormControl<boolean>(true),
  });

  viewVisible = false;
  viewLoading = false;
  viewDetail: ProjectMember | null = null;

  constructor(
    private service: ProjectMemberService,
    private appParamService: AppParamService,
    private store: StoreService,
    private projectService: ProjectService,
    private translate: TranslateService,
    private notification: NzNotificationService,
    private modal: NzModalService
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
    this.loadRoleOptions();
    this.modalForm.controls.startDate.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.modalForm.controls.endDate.updateValueAndValidity();
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
          this.translate.instant('projectMember.messages.missingProjectId')
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
      ? this.translate.instant('projectMember.form.createTitle')
      : this.translate.instant('projectMember.form.editTitle');
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
    this.resetModalForm();
    clearServerErrorsOnFormGroup(this.modalForm);
    this.formVisible = true;
  }

  onEdit(row: ProjectMember): void {
    if (!this.isProjectManager) return;
    const id = row?.id;
    if (!id) {
      this.notification.warning(
        this.translate.instant('common.error'),
        this.translate.instant('projectMember.messages.missingId')
      );
      return;
    }
    this.formMode = 'edit';
    this.editingId = id;
    this.formVisible = true;
    this.loading = true;
    this.resetModalForm();
    clearServerErrorsOnFormGroup(this.modalForm);
    this.service.getById(id).subscribe({
      next: ({ raw, row }) => {
        this.loading = false;
        if (raw?.code === 200 && row) {
          this.applyToForm(row);
        } else {
          this.notifyFromResponse(raw, this.translate.instant('projectMember.messages.loadFailed'));
          this.formVisible = false;
        }
      },
      error: () => {
        this.loading = false;
        this.formVisible = false;
      },
    });
  }

  onView(row: ProjectMember): void {
    const id = row?.id;
    if (!id) return;
    this.viewVisible = true;
    this.viewDetail = null;
    this.viewLoading = true;
    this.service.getById(id).subscribe({
      next: ({ raw, row }) => {
        this.viewLoading = false;
        if (raw?.code === 200 && row) {
          this.viewDetail = row;
        } else {
          this.notifyFromResponse(raw, this.translate.instant('projectMember.messages.loadFailed'));
          this.viewVisible = false;
        }
      },
      error: () => {
        this.viewLoading = false;
        this.viewVisible = false;
      },
    });
  }

  onDelete(row: ProjectMember): void {
    if (!this.isProjectManager) return;
    const id = row?.id;
    if (!id) {
      this.notification.warning(
        this.translate.instant('common.error'),
        this.translate.instant('projectMember.messages.missingId')
      );
      return;
    }
    const label = row.userDisplay?.trim() || id;
    this.modal.confirm({
      nzTitle: this.translate.instant('projectMember.messages.confirmDeleteTitle'),
      nzContent: this.translate.instant('projectMember.messages.confirmDeleteContent').replace('{{label}}', label),
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
                this.notifyFromResponse(raw, this.translate.instant('projectMember.messages.deleteFailed'));
                reject();
              }
            },
            error: () => reject(),
          });
        }),
    });
  }

  closeModal(): void {
    this.formVisible = false;
    clearServerErrorsOnFormGroup(this.modalForm);
  }

  closeView(): void {
    this.viewVisible = false;
    this.viewDetail = null;
  }

  save(): void {
    if (!this.isProjectManager) return;
    if (this.formMode === 'edit' && this.loading) return;
    clearServerErrorsOnFormGroup(this.modalForm);
    if (this.modalForm.invalid) {
      markFormControlsTouched(this.modalForm);
      return;
    }

    const v = this.modalForm.getRawValue();
    const payload: ProjectMemberWritePayload = {
      projectId: this.effectiveProjectId,
      userId: v.userId,
      roleInProject: v.roleInProject,
      allocationPercent: v.allocationPercent,
      isLead: v.isLead,
      startDate: v.startDate,
      endDate: v.endDate,
      active: v.active,
    };

    if (this.formMode === 'create') {
      this.submitting = true;
      this.service.create(payload).subscribe({
        next: ({ raw }) => {
          this.submitting = false;
          if (raw?.code === 201) {
            this.notification.success(this.translate.instant('common.button.done'), raw?.message ?? '');
            clearServerErrorsOnFormGroup(this.modalForm);
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
      this.notification.warning(
        this.translate.instant('common.error'),
        this.translate.instant('projectMember.messages.missingId')
      );
      return;
    }
    this.submitting = true;
    this.service.update(id, payload).subscribe({
      next: ({ raw }) => {
        this.submitting = false;
        if (raw?.code === 200) {
          this.notification.success(this.translate.instant('common.button.done'), raw?.message ?? '');
          clearServerErrorsOnFormGroup(this.modalForm);
          this.formVisible = false;
          this.fetch();
          return;
        }
        this.handleWriteError(raw);
      },
      error: () => (this.submitting = false),
    });
  }

  roleLabel(role?: ProjectMemberRole | null): string {
    if (!role) return '—';
    return this.roleLabelMap.get(role) ?? role;
  }

  asDate(v: string | Date | null | undefined): Date | null {
    if (v == null) return null;
    const d = v instanceof Date ? v : new Date(v);
    return Number.isNaN(d.getTime()) ? null : d;
  }

  private fetch(): void {
    if (!this.effectiveProjectId) {
      return;
    }
    const req: ProjectMemberSearchRequest = {
      keyword: this.keyword?.trim() || null,
      projectId: this.effectiveProjectId,
      roleInProject: this.roleInProject,
      active: this.active,
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

  private resetModalForm(): void {
    this.modalForm.reset({
      userId: null,
      roleInProject: 'DEVELOPER',
      allocationPercent: 100,
      isLead: false,
      startDate: null,
      endDate: null,
      active: true,
    });
  }

  private loadRoleOptions(): void {
    this.appParamService
      .getPage({
        pageIndex: 0,
        pageSize: 200,
        voided: false,
        paramGroup: 'MODULE_PROJECT_TEAM',
        paramType: 'PROJECT_TEAM',
      })
      .subscribe(({ raw, page }) => {
        if (raw?.code !== 200) return;
        const content = page?.content ?? [];
        const options = content
          .filter((x) => !!x?.paramValue)
          .map((x: AppParam) => ({
            value: String(x.paramValue) as ProjectMemberRole,
            label: (x.paramName ?? x.paramValue ?? '').toString(),
          }));
        this.roleOptions = options.length ? options : PROJECT_MEMBER_ROLE_OPTIONS;
        this.roleLabelMap = new Map(this.roleOptions.map((x) => [x.value as string, x.label]));
      });
  }

  private applyToForm(row: ProjectMember): void {
    this.modalForm.patchValue({
      userId: row.userId ?? null,
      roleInProject: (row.roleInProject ?? 'DEVELOPER') as ProjectMemberRole,
      allocationPercent: row.allocationPercent ?? null,
      isLead: !!row.isLead,
      startDate: this.asDate(row.startDate),
      endDate: this.asDate(row.endDate),
      active: row.active !== false,
    });
  }

  private handleWriteError(raw: ApiResponse | undefined): void {
    const map = parseServerFieldErrorMap(raw);
    if (map) {
      clearServerErrorsOnFormGroup(this.modalForm);
      applyServerFieldErrorsToFormGroup(this.modalForm, map);
      markFormControlsTouched(this.modalForm);
      return;
    }
    clearServerErrorsOnFormGroup(this.modalForm);
    this.notification.warning(this.translate.instant('common.error'), raw?.message ?? '');
  }

  private notifyFromResponse(raw: ApiResponse | undefined, fallback: string): void {
    this.notification.warning(this.translate.instant('common.error'), raw?.message ?? fallback);
  }
}
