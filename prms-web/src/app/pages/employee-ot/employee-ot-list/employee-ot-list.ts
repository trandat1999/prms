import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
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
import { EmployeeOt, EmployeeOtWritePayload } from '../models/employee-ot.model';
import { EmployeeOtSearchRequest } from '../models/employee-ot-search.request';
import { EmployeeOtStatus, EmployeeOtType, EMPLOYEE_OT_TYPE_COEFFICIENT } from '../models/employee-ot.types';
import { EmployeeOtService } from '../services/employee-ot.service';

type FormState = {
  userId: string | null;
  projectId: string | null;
  otDate: Date | null;
  startTime: Date | null;
  endTime: Date | null;
  otHours: number | null;
  otType: EmployeeOtType | null;
  reason: string | null;
  status: EmployeeOtStatus | null;
};

@Component({
  selector: 'app-employee-ot-list',
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
  templateUrl: './employee-ot-list.html',
  styleUrls: ['./employee-ot-list.scss'],
})
export class EmployeeOtList implements OnInit {
  keyword = '';
  filterUserId: string | null = null;
  filterProjectId: string | null = null;
  filterStatus: EmployeeOtStatus | null = null;
  filterOtType: EmployeeOtType | null = null;
  filterOtDateFrom: Date | null = null;
  filterOtDateTo: Date | null = null;
  reportMonth: Date | null = null;

  rows: EmployeeOt[] = [];
  page: Page<EmployeeOt> | null = null;
  pageIndex = 1;
  pageSize = 20;

  readonly userAutocompleteUrl = '/api/v1/autocomplete/users';
  readonly projectAutocompleteUrl = '/api/v1/autocomplete/projects';

  readonly statusFilterItems = [
    { id: EmployeeOtStatus.DRAFT, nameKey: 'employeeOt.status.DRAFT' },
    { id: EmployeeOtStatus.SUBMITTED, nameKey: 'employeeOt.status.SUBMITTED' },
    { id: EmployeeOtStatus.APPROVED, nameKey: 'employeeOt.status.APPROVED' },
    { id: EmployeeOtStatus.REJECTED, nameKey: 'employeeOt.status.REJECTED' },
  ];

  readonly otTypeFilterItems = [
    { id: EmployeeOtType.WEEKDAY, nameKey: 'employeeOt.otType.WEEKDAY' },
    { id: EmployeeOtType.WEEKEND, nameKey: 'employeeOt.otType.WEEKEND' },
    { id: EmployeeOtType.HOLIDAY, nameKey: 'employeeOt.otType.HOLIDAY' },
  ];

  readonly formStatusDraftItems = [
    { id: EmployeeOtStatus.DRAFT, nameKey: 'employeeOt.status.DRAFT' },
    { id: EmployeeOtStatus.SUBMITTED, nameKey: 'employeeOt.status.SUBMITTED' },
  ];

  readonly formStatusApprovalItems = [
    { id: EmployeeOtStatus.APPROVED, nameKey: 'employeeOt.status.APPROVED' },
    { id: EmployeeOtStatus.REJECTED, nameKey: 'employeeOt.status.REJECTED' },
  ];

  readonly formOtTypeItems = this.otTypeFilterItems;

  formVisible = false;
  formMode: 'create' | 'edit' = 'create';
  editingId: string | null = null;
  /** Bản ghi đang sửa — trạng thái SUBMITTED chỉ cho phép duyệt/từ chối */
  editingRowStatus: EmployeeOtStatus | null = null;
  submitting = false;
  loading = false;
  form: FormState = this.emptyForm();

  viewVisible = false;
  viewLoading = false;
  viewDetail: EmployeeOt | null = null;

  constructor(
    private service: EmployeeOtService,
    private translate: TranslateService,
    private notification: NzNotificationService,
    private modal: NzModalService
  ) {}

  ngOnInit(): void {
    this.fetch();
  }

  get formTitle(): string {
    return this.formMode === 'create'
      ? this.translate.instant('employeeOt.form.createTitle')
      : this.translate.instant('employeeOt.form.editTitle');
  }

  get formCoreDisabled(): boolean {
    return this.formMode === 'edit' && this.editingRowStatus === EmployeeOtStatus.SUBMITTED;
  }

  get formStatusItems(): { id: EmployeeOtStatus; nameKey: string }[] {
    if (this.formCoreDisabled) {
      return this.formStatusApprovalItems;
    }
    if (this.editingRowStatus === EmployeeOtStatus.REJECTED) {
      return [
        ...this.formStatusDraftItems,
        { id: EmployeeOtStatus.REJECTED, nameKey: 'employeeOt.status.REJECTED' },
      ];
    }
    return this.formStatusDraftItems;
  }

  coefficientFor(type: EmployeeOtType | string | null | undefined): number | null {
    if (!type) return null;
    const t = type as EmployeeOtType;
    return EMPLOYEE_OT_TYPE_COEFFICIENT[t] ?? null;
  }

  onSearch(): void {
    this.pageIndex = 1;
    this.fetch();
  }

  onExportMonthlyReport(): void {
    if (!this.reportMonth) {
      this.notification.warning(
        this.translate.instant('employeeOt.messages.invalidTitle'),
        this.translate.instant('employeeOt.messages.reportMonthRequired')
      );
      return;
    }
    const ym = this.formatYearMonth(this.reportMonth);
    this.service
      .exportMonthlyReport({
        month: ym,
        keyword: this.keyword?.trim() || null,
        userId: this.filterUserId,
        projectId: this.filterProjectId,
        status: this.filterStatus,
        otType: this.filterOtType,
      })
      .subscribe({
      next: (res) => {
        const blob = res?.body;
        if (!blob) return;
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `ot-report-${ym}.docx`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
    });
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
    this.editingRowStatus = null;
    this.loading = false;
    this.form = this.emptyForm();
    this.form.status = EmployeeOtStatus.DRAFT;
    this.formVisible = true;
  }

  onEdit(row: EmployeeOt): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning(
        this.translate.instant('common.error'),
        this.translate.instant('employeeOt.messages.missingId')
      );
      return;
    }
    this.formMode = 'edit';
    this.editingId = id;
    this.editingRowStatus = row.status ?? null;
    this.formVisible = true;
    this.loading = true;
    this.form = this.emptyForm();
    this.service.getById(id).subscribe({
      next: ({ raw, row: r }) => {
        this.loading = false;
        if (raw?.code === 200 && r) {
          this.editingRowStatus = r.status ?? null;
          this.applyToForm(r);
          if (this.formCoreDisabled) {
            this.form.status = EmployeeOtStatus.APPROVED;
          }
        } else {
          this.notifyFromResponse(raw, this.translate.instant('employeeOt.messages.loadFailed'));
          this.formVisible = false;
        }
      },
      error: () => {
        this.loading = false;
        this.formVisible = false;
      },
    });
  }

  onView(row: EmployeeOt): void {
    const id = row?.id;
    if (!id) return;
    this.viewVisible = true;
    this.viewDetail = null;
    this.viewLoading = true;
    this.service.getById(id).subscribe({
      next: ({ raw, row: r }) => {
        this.viewLoading = false;
        if (raw?.code === 200 && r) {
          this.viewDetail = r;
        } else {
          this.notifyFromResponse(raw, this.translate.instant('employeeOt.messages.loadFailed'));
          this.viewVisible = false;
        }
      },
      error: () => {
        this.viewLoading = false;
        this.viewVisible = false;
      },
    });
  }

  onDelete(row: EmployeeOt): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning(
        this.translate.instant('common.error'),
        this.translate.instant('employeeOt.messages.missingId')
      );
      return;
    }
    const label = row.userDisplay?.trim() || id;
    this.modal.confirm({
      nzTitle: this.translate.instant('employeeOt.messages.confirmDeleteTitle'),
      nzContent: this.translate.instant('employeeOt.messages.confirmDeleteContent').replace('{{label}}', label),
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
                this.notifyFromResponse(raw, this.translate.instant('employeeOt.messages.deleteFailed'));
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

    const payload: EmployeeOtWritePayload = {
      userId: this.form.userId as string,
      projectId: this.form.projectId?.trim() ? this.form.projectId : null,
      otDate: this.form.otDate as Date,
      startTime: this.form.startTime ?? null,
      endTime: this.form.endTime ?? null,
      otHours: this.form.otHours ?? null,
      otType: this.form.otType as EmployeeOtType,
      reason: this.form.reason?.trim() ? this.form.reason.trim() : null,
      status: this.form.status ?? EmployeeOtStatus.DRAFT,
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
      this.notification.warning(
        this.translate.instant('common.error'),
        this.translate.instant('employeeOt.messages.missingId')
      );
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
    const req: EmployeeOtSearchRequest = {
      keyword: this.keyword?.trim() || null,
      userId: this.filterUserId,
      projectId: this.filterProjectId,
      status: this.filterStatus,
      otType: this.filterOtType,
      otDateFrom: this.filterOtDateFrom ?? null,
      otDateTo: this.filterOtDateTo ?? null,
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
      projectId: null,
      otDate: null,
      startTime: null,
      endTime: null,
      otHours: null,
      otType: EmployeeOtType.WEEKDAY,
      reason: null,
      status: EmployeeOtStatus.DRAFT,
    };
  }

  private applyToForm(row: EmployeeOt): void {
    this.form = {
      userId: row.userId ?? null,
      projectId: row.projectId ?? null,
      otDate: this.asDate(row.otDate),
      startTime: this.asDate(row.startTime ?? null),
      endTime: this.asDate(row.endTime ?? null),
      otHours: row.otHours ?? null,
      otType: (row.otType as EmployeeOtType) ?? EmployeeOtType.WEEKDAY,
      reason: row.reason ?? null,
      status: row.status ?? EmployeeOtStatus.DRAFT,
    };
  }

  private validate(): boolean {
    if (!this.form.userId) {
      this.notification.warning(
        this.translate.instant('employeeOt.messages.invalidTitle'),
        this.translate.instant('employeeOt.messages.userRequired')
      );
      return false;
    }
    if (!this.form.otDate) {
      this.notification.warning(
        this.translate.instant('employeeOt.messages.invalidTitle'),
        this.translate.instant('employeeOt.messages.otDateRequired')
      );
      return false;
    }
    if (!this.form.otType) {
      this.notification.warning(
        this.translate.instant('employeeOt.messages.invalidTitle'),
        this.translate.instant('employeeOt.messages.otTypeRequired')
      );
      return false;
    }
    if (this.form.otHours != null && this.form.otHours < 0) {
      this.notification.warning(
        this.translate.instant('employeeOt.messages.invalidTitle'),
        this.translate.instant('employeeOt.messages.otHoursInvalid')
      );
      return false;
    }
    if (this.form.startTime && this.form.endTime && this.form.startTime > this.form.endTime) {
      this.notification.warning(
        this.translate.instant('employeeOt.messages.invalidTitle'),
        this.translate.instant('employeeOt.messages.timeRangeInvalid')
      );
      return false;
    }
    if (!this.form.status) {
      this.notification.warning(
        this.translate.instant('employeeOt.messages.invalidTitle'),
        this.translate.instant('employeeOt.messages.statusRequired')
      );
      return false;
    }
    return true;
  }

  private handleWriteError(raw: ApiResponse | undefined): void {
    const errBody = raw?.body;
    if (raw?.code === 400 && errBody && typeof errBody === 'object' && !Array.isArray(errBody)) {
      const msg = Object.values(errBody as Record<string, string>).filter(Boolean).join(' ');
      this.notification.warning(this.translate.instant('employeeOt.messages.invalidTitle'), msg || raw?.message || '');
      return;
    }
    this.notification.warning(this.translate.instant('common.error'), raw?.message ?? '');
  }

  private notifyFromResponse(raw: ApiResponse | undefined, fallback: string): void {
    this.notification.warning(this.translate.instant('common.error'), raw?.message ?? fallback);
  }

  private formatYearMonth(d: Date): string {
    const y = d.getFullYear();
    const m = d.getMonth() + 1;
    return `${y}-${String(m).padStart(2, '0')}`;
  }
}
