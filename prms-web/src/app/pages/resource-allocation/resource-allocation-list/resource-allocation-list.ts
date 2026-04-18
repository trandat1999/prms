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
import { InputCommon } from '../../../shared/input/input';
import { ApiResponse } from '../../../shared/utils/api-response';
import { Page } from '../../project/models/page.model';
import { ResourceAllocation, ResourceAllocationWritePayload } from '../models/resource-allocation.model';
import { ResourceAllocationSearchRequest } from '../models/resource-allocation-search.request';
import { ResourceAllocationService } from '../services/resource-allocation.service';

type FormState = {
  userId: string | null;
  role: string | null;
  month: Date | null;
  startDate: Date | null;
  endDate: Date | null;
  allocationPercent: number | null;
};

@Component({
  selector: 'app-resource-allocation-list',
  imports: [
    CommonModule,
    DatePipe,
    FormsModule,
    NzTableModule,
    NzButtonComponent,
    NzIconDirective,
    NzModalModule,
    NzSpinComponent,
    InputCommon,
    TranslatePipe,
  ],
  templateUrl: './resource-allocation-list.html',
  styleUrls: ['./resource-allocation-list.scss'],
})
export class ResourceAllocationList {
  keyword = '';
  filterUserId: string | null = null;
  filterRole: string | null = null;
  filterMonth: Date | null = null;

  rows: ResourceAllocation[] = [];
  page: Page<ResourceAllocation> | null = null;
  pageIndex = 1;
  pageSize = 20;

  readonly userAutocompleteUrl = '/api/v1/autocomplete/users';
  readonly appParamPageUrl = '/api/v1/app-params/page';
  readonly roleAppParamFilters = { paramGroup: 'MODULE_RESOURCE_ALLOCATION', paramType: 'RESOURCE_ALLOCATION' };

  formVisible = false;
  formMode: 'create' | 'edit' = 'create';
  editingId: string | null = null;
  submitting = false;
  loading = false;
  form: FormState = this.emptyForm();

  viewVisible = false;
  viewLoading = false;
  viewDetail: ResourceAllocation | null = null;

  constructor(
    private service: ResourceAllocationService,
    private translate: TranslateService,
    private notification: NzNotificationService,
    private modal: NzModalService
  ) {}

  ngOnInit(): void {
    this.fetch();
  }

  get formTitle(): string {
    return this.formMode === 'create'
      ? this.translate.instant('resourceAllocation.form.createTitle')
      : this.translate.instant('resourceAllocation.form.editTitle');
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

  onEdit(row: ResourceAllocation): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning(this.translate.instant('common.error'), this.translate.instant('resourceAllocation.messages.missingId'));
      return;
    }
    this.formMode = 'edit';
    this.editingId = id;
    this.formVisible = true;
    this.loading = true;
    this.form = this.emptyForm();
    this.service.getById(id).subscribe({
      next: ({ raw, row }) => {
        this.loading = false;
        if (raw?.code === 200 && row) {
          this.applyToForm(row);
        } else {
          this.notifyFromResponse(raw, this.translate.instant('resourceAllocation.messages.loadFailed'));
          this.formVisible = false;
        }
      },
      error: () => {
        this.loading = false;
        this.formVisible = false;
      },
    });
  }

  onView(row: ResourceAllocation): void {
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
          this.notifyFromResponse(raw, this.translate.instant('resourceAllocation.messages.loadFailed'));
          this.viewVisible = false;
        }
      },
      error: () => {
        this.viewLoading = false;
        this.viewVisible = false;
      },
    });
  }

  onDelete(row: ResourceAllocation): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning(this.translate.instant('common.error'), this.translate.instant('resourceAllocation.messages.missingId'));
      return;
    }
    const label = row.userDisplay?.trim() || id;
    this.modal.confirm({
      nzTitle: this.translate.instant('resourceAllocation.messages.confirmDeleteTitle'),
      nzContent: this.translate.instant('resourceAllocation.messages.confirmDeleteContent').replace('{{label}}', label),
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
                this.notifyFromResponse(raw, this.translate.instant('resourceAllocation.messages.deleteFailed'));
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
  }

  closeView(): void {
    this.viewVisible = false;
    this.viewDetail = null;
  }

  save(): void {
    if (this.formMode === 'edit' && this.loading) return;
    if (!this.validate()) return;

    const payload: ResourceAllocationWritePayload = {
      userId: this.form.userId as string,
      role: (this.form.role ?? '').trim(),
      month: this.form.month as Date,
      startDate: this.form.startDate ?? null,
      endDate: this.form.endDate ?? null,
      allocationPercent: Number(this.form.allocationPercent),
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
      this.notification.warning(this.translate.instant('common.error'), this.translate.instant('resourceAllocation.messages.missingId'));
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

  asDate(v: string | Date | null | undefined): Date | null {
    if (v == null) return null;
    const d = v instanceof Date ? v : new Date(v);
    return Number.isNaN(d.getTime()) ? null : d;
  }

  private fetch(): void {
    const req: ResourceAllocationSearchRequest = {
      keyword: this.keyword?.trim() || null,
      userId: this.filterUserId,
      role: this.filterRole?.trim() || null,
      month: this.filterMonth ?? null,
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

  private emptyForm(): FormState {
    return {
      userId: null,
      role: null,
      month: null,
      startDate: null,
      endDate: null,
      allocationPercent: 100,
    };
  }

  private applyToForm(row: ResourceAllocation): void {
    this.form = {
      userId: row.userId ?? null,
      role: row.role ?? null,
      month: this.asDate(row.month),
      startDate: this.asDate(row.startDate ?? null),
      endDate: this.asDate(row.endDate ?? null),
      allocationPercent: row.allocationPercent ?? null,
    };
  }

  private validate(): boolean {
    if (!this.form.userId) {
      this.notification.warning(
        this.translate.instant('resourceAllocation.messages.invalidTitle'),
        this.translate.instant('resourceAllocation.messages.userRequired')
      );
      return false;
    }
    if (!this.form.role?.trim()) {
      this.notification.warning(
        this.translate.instant('resourceAllocation.messages.invalidTitle'),
        this.translate.instant('resourceAllocation.messages.roleRequired')
      );
      return false;
    }
    if (!this.form.month) {
      this.notification.warning(
        this.translate.instant('resourceAllocation.messages.invalidTitle'),
        this.translate.instant('resourceAllocation.messages.monthRequired')
      );
      return false;
    }
    if (this.form.allocationPercent == null || this.form.allocationPercent < 0 || this.form.allocationPercent > 100) {
      this.notification.warning(
        this.translate.instant('resourceAllocation.messages.invalidTitle'),
        this.translate.instant('resourceAllocation.messages.percentInvalid')
      );
      return false;
    }
    if (this.form.startDate && this.form.endDate && this.form.startDate > this.form.endDate) {
      this.notification.warning(
        this.translate.instant('resourceAllocation.messages.invalidTitle'),
        this.translate.instant('resourceAllocation.messages.dateRangeInvalid')
      );
      return false;
    }
    return true;
  }

  private handleWriteError(raw: ApiResponse | undefined): void {
    const errBody = raw?.body;
    if (raw?.code === 400 && errBody && typeof errBody === 'object' && !Array.isArray(errBody)) {
      const msg = Object.values(errBody as Record<string, string>).filter(Boolean).join(' ');
      this.notification.warning(this.translate.instant('resourceAllocation.messages.invalidTitle'), msg || raw?.message || '');
      return;
    }
    this.notification.warning(this.translate.instant('common.error'), raw?.message ?? '');
  }

  private notifyFromResponse(raw: ApiResponse | undefined, fallback: string): void {
    this.notification.warning(this.translate.instant('common.error'), raw?.message ?? fallback);
  }
}
