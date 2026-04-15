import { CommonModule, DatePipe } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NzAvatarComponent } from 'ng-zorro-antd/avatar';
import { NzButtonComponent } from 'ng-zorro-antd/button';
import { NzIconDirective } from 'ng-zorro-antd/icon';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { NzProgressComponent } from 'ng-zorro-antd/progress';
import { NzSpinComponent } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagComponent } from 'ng-zorro-antd/tag';
import { NzTooltipDirective } from 'ng-zorro-antd/tooltip';
import { InputCommon } from '../../../shared/input/input';
import { Page } from '../models/page.model';
import { Project, ProjectWritePayload } from '../models/project.model';
import { ProjectSearchRequest } from '../models/project-search.request';
import {
  PROJECT_PRIORITY_OPTIONS,
  projectPriorityColor,
  projectPriorityLabel,
  PROJECT_STATUS_OPTIONS,
  projectStatusColor,
  projectStatusLabel,
} from '../models/project.const';
import { PriorityEnum, ProjectStatusEnum } from '../models/project.types';
import { ProjectService } from '../services/project.service';
import { ApiResponse } from '../../../shared/utils/api-response';

type ProjectCreateFormState = {
  code: string;
  name: string;
  shortDescription: string;
  description: string;
  managerId: string | null;
  priority: PriorityEnum | null;
  startDate: Date | null;
  endDate: Date | null;
  status: ProjectStatusEnum | null;
  progressPercentage: number | null;
};

@Component({
  selector: 'app-project-list',
  imports: [
    CommonModule,
    DatePipe,
    FormsModule,
    NzTableModule,
    NzAvatarComponent,
    NzTagComponent,
    NzProgressComponent,
    NzTooltipDirective,
    NzButtonComponent,
    NzIconDirective,
    NzModalModule,
    NzSpinComponent,
    InputCommon,
  ],
  templateUrl: './project-list.html',
  styleUrl: './project-list.scss',
})
export class ProjectList {
  keyword = '';
  month: Date | null = null;
  status: ProjectStatusEnum | null = null;

  readonly statuses: ProjectStatusEnum[] = PROJECT_STATUS_OPTIONS.map((x) => x.value);
  readonly priorityOptions = PROJECT_PRIORITY_OPTIONS;
  readonly statusOptionsForForm = PROJECT_STATUS_OPTIONS;
  readonly managerAutocompleteUrl = '/api/v1/autocomplete/users';

  rows: Project[] = [];
  page: Page<Project> | null = null;

  /** Modal thêm / sửa */
  projectFormVisible = false;
  projectFormMode: 'create' | 'edit' = 'create';
  editingProjectId: string | null = null;
  projectFormSubmitting = false;
  projectFormLoading = false;
  createForm: ProjectCreateFormState = this.emptyCreateForm();

  /** Modal xem */
  viewVisible = false;
  viewLoading = false;
  viewDetail: Project | null = null;

  constructor(
    private projectService: ProjectService,
    private notification: NzNotificationService,
    private modal: NzModalService
  ) {}

  get projectFormTitle(): string {
    return this.projectFormMode === 'create' ? 'Thêm dự án mới' : 'Sửa dự án';
  }

  ngOnInit(): void {
    this.fetch();
  }

  onSearch(): void {
    this.fetch();
  }

  onCreate(): void {
    this.projectFormMode = 'create';
    this.editingProjectId = null;
    this.projectFormLoading = false;
    this.resetCreateForm();
    this.projectFormVisible = true;
  }

  onEdit(row: Project): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning('Lỗi', 'Không có mã dự án để sửa.');
      return;
    }
    this.projectFormMode = 'edit';
    this.editingProjectId = id;
    this.projectFormVisible = true;
    this.projectFormLoading = true;
    this.resetCreateForm();
    this.projectService.getById(id).subscribe({
      next: ({ raw, project }) => {
        this.projectFormLoading = false;
        if (raw?.code === 200 && project) {
          this.applyProjectToForm(project);
        } else {
          this.notifyFromResponse(raw, 'Không tải được dự án.');
          this.projectFormVisible = false;
        }
      },
      error: () => {
        this.projectFormLoading = false;
        this.projectFormVisible = false;
      },
    });
  }

  onView(row: Project): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning('Lỗi', 'Không có mã dự án để xem.');
      return;
    }
    this.viewVisible = true;
    this.viewDetail = null;
    this.viewLoading = true;
    this.projectService.getById(id).subscribe({
      next: ({ raw, project }) => {
        this.viewLoading = false;
        if (raw?.code === 200 && project) {
          this.viewDetail = project;
        } else {
          this.notifyFromResponse(raw, 'Không tải được chi tiết dự án.');
          this.viewVisible = false;
        }
      },
      error: () => {
        this.viewLoading = false;
        this.viewVisible = false;
      },
    });
  }

  onDelete(row: Project): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning('Lỗi', 'Không có mã dự án để xóa.');
      return;
    }
    const label = row.name?.trim() || row.code || id;
    this.modal.confirm({
      nzTitle: 'Xác nhận xóa',
      nzContent: `Bạn có chắc muốn xóa dự án "${label}"? Dự án sẽ được đánh dấu xóa (soft delete) nếu hệ thống hỗ trợ.`,
      nzOkText: 'Xóa',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzCancelText: 'Hủy',
      nzOnOk: () =>
        new Promise<void>((resolve, reject) => {
          this.projectService.delete(id).subscribe({
            next: ({ raw }) => {
              if (raw?.code === 200) {
                this.notification.success('Đã xóa', raw?.message ?? 'Xóa dự án thành công.');
                this.fetch();
                resolve();
              } else {
                this.notifyFromResponse(raw, 'Không xóa được dự án.');
                reject();
              }
            },
            error: () => reject(),
          });
        }),
    });
  }

  closeProjectFormModal(): void {
    this.projectFormVisible = false;
  }

  closeViewModal(): void {
    this.viewVisible = false;
    this.viewDetail = null;
  }

  saveProjectForm(): void {
    if (this.projectFormMode === 'edit' && this.projectFormLoading) {
      return;
    }
    if (!this.validateCreateForm()) {
      return;
    }
    const payload = this.buildCreatePayload();
    if (this.projectFormMode === 'create') {
      this.submitCreate(payload);
      return;
    }
    const editId = this.editingProjectId;
    if (!editId) {
      this.notification.warning('Lỗi', 'Thiếu mã dự án khi cập nhật.');
      return;
    }
    this.submitUpdate(editId, payload);
  }

  priorityTagColor(p: PriorityEnum | undefined): string {
    return projectPriorityColor(p);
  }

  statusTagColor(s: ProjectStatusEnum | undefined): string {
    return projectStatusColor(s);
  }

  priorityLabel(p: PriorityEnum | undefined): string {
    return projectPriorityLabel(p);
  }

  statusLabel(s: ProjectStatusEnum | undefined): string {
    return projectStatusLabel(s);
  }

  initials(name?: string | null): string {
    if (!name) return '';
    const parts = name.trim().split(/\s+/).slice(0, 2);
    return parts.map((x) => x[0]?.toUpperCase()).join('');
  }

  progressPercent(item: Project): number {
    const p = item.progressPercentage;
    if (p === null || p === undefined || Number.isNaN(Number(p))) {
      return 0;
    }
    return Math.min(100, Math.max(0, Number(p)));
  }

  private submitCreate(payload: ProjectWritePayload): void {
    this.projectFormSubmitting = true;
    this.projectService.create(payload).subscribe({
      next: ({ raw }) => {
        this.projectFormSubmitting = false;
        if (raw?.code === 201) {
          this.notification.success('Thành công', raw?.message ?? 'Đã tạo dự án.');
          this.projectFormVisible = false;
          this.fetch();
          return;
        }
        this.handleWriteError(raw);
      },
      error: () => {
        this.projectFormSubmitting = false;
      },
    });
  }

  private submitUpdate(id: string, payload: ProjectWritePayload): void {
    this.projectFormSubmitting = true;
    this.projectService.update(id, payload).subscribe({
      next: ({ raw }) => {
        this.projectFormSubmitting = false;
        if (raw?.code === 200) {
          this.notification.success('Thành công', raw?.message ?? 'Đã cập nhật dự án.');
          this.projectFormVisible = false;
          this.fetch();
          return;
        }
        this.handleWriteError(raw);
      },
      error: () => {
        this.projectFormSubmitting = false;
      },
    });
  }

  private handleWriteError(raw: ApiResponse | undefined): void {
    const errBody = raw?.body;
    if (raw?.code === 400 && errBody && typeof errBody === 'object' && !Array.isArray(errBody)) {
      const msg = Object.values(errBody as Record<string, string>).filter(Boolean).join(' ');
      this.notification.warning('Không hợp lệ', msg || raw?.message || 'Vui lòng kiểm tra dữ liệu.');
      return;
    }
    this.notification.warning('Không thành công', raw?.message ?? 'Thao tác thất bại.');
  }

  private notifyFromResponse(raw: ApiResponse | undefined, fallback: string): void {
    this.notification.warning('Thông báo', raw?.message ?? fallback);
  }

  private emptyCreateForm(): ProjectCreateFormState {
    return {
      code: '',
      name: '',
      shortDescription: '',
      description: '',
      managerId: null,
      priority: 'MEDIUM',
      startDate: null,
      endDate: null,
      status: 'NOT_STARTED',
      progressPercentage: 0,
    };
  }

  private resetCreateForm(): void {
    this.createForm = this.emptyCreateForm();
  }

  private applyProjectToForm(p: Project): void {
    this.createForm = {
      code: p.code ?? '',
      name: p.name ?? '',
      shortDescription: p.shortDescription ?? '',
      description: p.description ?? '',
      managerId: p.managerId ?? null,
      priority: (p.priority as PriorityEnum) ?? 'MEDIUM',
      startDate: p.startDate ? new Date(p.startDate as string | Date) : null,
      endDate: p.endDate ? new Date(p.endDate as string | Date) : null,
      status: (p.status as ProjectStatusEnum) ?? 'NOT_STARTED',
      progressPercentage:
        p.progressPercentage === null || p.progressPercentage === undefined
          ? 0
          : Number(p.progressPercentage),
    };
  }

  private validateCreateForm(): boolean {
    const code = this.createForm.code?.trim();
    const name = this.createForm.name?.trim();
    if (!code) {
      this.notification.warning('Thiếu dữ liệu', 'Vui lòng nhập mã dự án.');
      return false;
    }
    if (!name) {
      this.notification.warning('Thiếu dữ liệu', 'Vui lòng nhập tên dự án.');
      return false;
    }
    return true;
  }

  private buildCreatePayload(): ProjectWritePayload {
    const f = this.createForm;
    const toIso = (d: Date | null): string | null => (d ? new Date(d).toISOString() : null);
    return {
      code: f.code?.trim() || undefined,
      name: f.name?.trim() || undefined,
      shortDescription: f.shortDescription?.trim() ? f.shortDescription.trim() : null,
      description: f.description?.trim() ? f.description.trim() : null,
      managerId: f.managerId || null,
      priority: f.priority ?? null,
      startDate: toIso(f.startDate),
      endDate: toIso(f.endDate),
      status: f.status ?? null,
      progressPercentage:
        f.progressPercentage === null || f.progressPercentage === undefined
          ? null
          : Number(f.progressPercentage),
    };
  }

  private fetch(): void {
    const req: ProjectSearchRequest = {
      keyword: this.keyword?.trim() || null,
      pageIndex: 0,
      pageSize: 20,
      voided: false,
    };

    this.projectService.getPage(req).subscribe(({ page }) => {
      this.page = page;
      const content = page?.content ?? [];
      this.rows = this.applyClientFilters(content);
    });
  }

  private applyClientFilters(items: Project[]): Project[] {
    const month = this.month ? { y: this.month.getFullYear(), m: this.month.getMonth() } : null;
    const status = this.status;

    return items.filter((x) => {
      const matchesMonth = !month || this.sameMonth(x?.startDate, month.y, month.m);
      const matchesStatus = !status || x?.status === status;
      return matchesMonth && matchesStatus;
    });
  }

  private sameMonth(dateLike: string | Date | undefined, y: number, m: number): boolean {
    if (!dateLike) return false;
    const d = dateLike instanceof Date ? dateLike : new Date(dateLike);
    return d.getFullYear() === y && d.getMonth() === m;
  }
}
