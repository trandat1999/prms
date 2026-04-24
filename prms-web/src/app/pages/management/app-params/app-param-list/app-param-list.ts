import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { NzButtonComponent } from 'ng-zorro-antd/button';
import { NzIconDirective } from 'ng-zorro-antd/icon';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { NzSpinComponent } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
import { InputCommon } from '../../../../shared/input/input';
import { ApiResponse } from '../../../../shared/utils/api-response';
import {
  applyServerFieldErrorsToFormGroup,
  clearServerErrorsOnFormGroup,
  parseServerFieldErrorMap,
} from '../../../../shared/utils/form-server-errors';
import { markFormControlsTouched, trimRequiredValidator } from '../../../../shared/utils/form-validation';
import { Page } from '../../../project/models/page.model';
import { AppParam, AppParamWritePayload } from '../models/app-param.model';
import { AppParamSearchRequest } from '../models/app-param-search.request';
import { AppParamService } from '../services/app-param.service';

@Component({
  selector: 'app-app-param-list',
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    NzTableModule,
    NzButtonComponent,
    NzIconDirective,
    NzModalModule,
    NzSpinComponent,
    InputCommon,
    TranslatePipe,
  ],
  templateUrl: './app-param-list.html',
  styleUrl: './app-param-list.scss',
})
export class AppParamList {
  keyword = '';
  group = '';
  type = '';

  rows: AppParam[] = [];
  page: Page<AppParam> | null = null;

  pageIndex = 1;
  pageSize = 20;

  /** Modal thêm / sửa */
  formVisible = false;
  formMode: 'create' | 'edit' = 'create';
  editingId: string | null = null;
  submitting = false;
  loading = false;
  readonly modalForm = new FormGroup({
    paramGroup: new FormControl<string>('', [Validators.required, trimRequiredValidator]),
    paramName: new FormControl<string>('', [Validators.required, trimRequiredValidator]),
    paramValue: new FormControl<string>(''),
    paramType: new FormControl<string>(''),
    description: new FormControl<string>(''),
  });

  constructor(
    private service: AppParamService,
    private translate: TranslateService,
    private notification: NzNotificationService,
    private modal: NzModalService
  ) {}

  ngOnInit(): void {
    this.fetch();
  }

  get formTitle(): string {
    return this.formMode === 'create'
      ? this.t('appParam.form.createTitle')
      : this.t('appParam.form.editTitle');
  }

  onSearch(): void {
    this.pageIndex = 1;
    this.fetch();
  }

  onCreate(): void {
    this.formMode = 'create';
    this.editingId = null;
    this.loading = false;
    this.resetModalForm();
    clearServerErrorsOnFormGroup(this.modalForm);
    this.formVisible = true;
  }

  onEdit(row: AppParam): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning(this.t('common.error'), this.t('appParam.messages.missingId'));
      return;
    }
    this.formMode = 'edit';
    this.editingId = id;
    this.formVisible = true;
    this.loading = true;
    this.resetModalForm();
    clearServerErrorsOnFormGroup(this.modalForm);
    this.service.getById(id).subscribe({
      next: ({ raw, item }) => {
        this.loading = false;
        if (raw?.code === 200 && item) {
          this.applyToForm(item);
        } else {
          this.notifyFromResponse(raw, this.t('appParam.messages.loadFailed'));
          this.formVisible = false;
        }
      },
      error: () => {
        this.loading = false;
        this.formVisible = false;
      },
    });
  }

  onDelete(row: AppParam): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning(this.t('common.error'), this.t('appParam.messages.missingId'));
      return;
    }
    const label = row.paramName?.trim() || id;
    this.modal.confirm({
      nzTitle: this.t('appParam.messages.confirmDeleteTitle'),
      nzContent: this.t('appParam.messages.confirmDeleteContent').replace('{{label}}', label),
      nzOkText: this.t('common.button.delete'),
      nzOkDanger: true,
      nzCancelText: this.t('common.button.cancel'),
      nzOnOk: () =>
        new Promise<void>((resolve, reject) => {
          this.service.delete(id).subscribe({
            next: ({ raw }) => {
              if (raw?.code === 200) {
                this.notification.success(this.t('common.button.done'), raw?.message ?? this.t('appParam.messages.deleted'));
                this.fetch();
                resolve();
              } else {
                this.notifyFromResponse(raw, this.t('appParam.messages.deleteFailed'));
                reject();
              }
            },
            error: () => reject(),
          });
        }),
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

  closeModal(): void {
    this.formVisible = false;
    clearServerErrorsOnFormGroup(this.modalForm);
  }

  save(): void {
    if (this.formMode === 'edit' && this.loading) return;
    clearServerErrorsOnFormGroup(this.modalForm);
    if (this.modalForm.invalid) {
      markFormControlsTouched(this.modalForm);
      return;
    }

    const v = this.modalForm.getRawValue();
    const payload: AppParamWritePayload = {
      paramGroup: v.paramGroup.trim(),
      paramName: v.paramName.trim(),
      paramValue: v.paramValue?.trim() ? v.paramValue.trim() : null,
      paramType: v.paramType?.trim() ? v.paramType.trim() : null,
      description: v.description?.trim() ? v.description.trim() : null,
    };

    if (this.formMode === 'create') {
      this.submitting = true;
      this.service.create(payload).subscribe({
        next: ({ raw }) => {
          this.submitting = false;
          if (raw?.code === 201) {
            this.notification.success(this.t('common.button.done'), raw?.message ?? this.t('appParam.messages.created'));
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
      this.notification.warning(this.t('common.error'), this.t('appParam.messages.missingId'));
      return;
    }
    this.submitting = true;
    this.service.update(id, payload).subscribe({
      next: ({ raw }) => {
        this.submitting = false;
        if (raw?.code === 200) {
          this.notification.success(this.t('common.button.done'), raw?.message ?? this.t('appParam.messages.updated'));
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

  private fetch(): void {
    const req: AppParamSearchRequest = {
      keyword: this.keyword?.trim() || null,
      paramGroup: this.group?.trim() || null,
      paramType: this.type?.trim() || null,
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

  private resetModalForm(): void {
    this.modalForm.reset({
      paramGroup: '',
      paramName: '',
      paramValue: '',
      paramType: '',
      description: '',
    });
  }

  private applyToForm(item: AppParam): void {
    this.modalForm.patchValue({
      paramGroup: item.paramGroup ?? '',
      paramName: item.paramName ?? '',
      paramValue: (item.paramValue ?? '') as string,
      paramType: (item.paramType ?? '') as string,
      description: (item.description ?? '') as string,
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
    this.notification.warning(this.t('common.error'), raw?.message ?? this.t('appParam.messages.actionFailed'));
  }

  private notifyFromResponse(raw: ApiResponse | undefined, fallback: string): void {
    this.notification.warning(this.t('common.error'), raw?.message ?? fallback);
  }

  /** helper đơn giản cho template/title; TranslatePipe đã có trong template */
  private t(key: string): string {
    return this.translate.instant(key);
  }
}
