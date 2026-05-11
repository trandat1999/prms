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
import { Skill, SkillWritePayload } from '../models/skill.model';
import { SkillSearchRequest } from '../models/skill-search.request';
import { SkillService } from '../services/skill.service';

@Component({
  selector: 'app-skill-list',
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
  templateUrl: './skill-list.html',
  styleUrl: './skill-list.scss',
})
export class SkillList {
  keyword = '';
  category = '';

  rows: Skill[] = [];
  page: Page<Skill> | null = null;

  pageIndex = 1;
  pageSize = 20;

  formVisible = false;
  formMode: 'create' | 'edit' = 'create';
  editingId: string | null = null;
  submitting = false;
  loading = false;

  readonly modalForm = new FormGroup({
    code: new FormControl<string>('', [Validators.required, trimRequiredValidator]),
    name: new FormControl<string>('', [Validators.required, trimRequiredValidator]),
    category: new FormControl<string>(''),
    description: new FormControl<string>(''),
  });

  constructor(
    private service: SkillService,
    private translate: TranslateService,
    private notification: NzNotificationService,
    private modal: NzModalService
  ) {}

  ngOnInit(): void {
    this.fetch();
  }

  get formTitle(): string {
    return this.formMode === 'create'
      ? this.translate.instant('skill.form.createTitle')
      : this.translate.instant('skill.form.editTitle');
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

  onEdit(row: Skill): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning(this.t('common.error'), this.t('skill.messages.missingId'));
      return;
    }
    this.formMode = 'edit';
    this.editingId = id;
    this.formVisible = true;
    this.loading = true;
    this.resetModalForm();
    clearServerErrorsOnFormGroup(this.modalForm);
    this.service.getById(id).subscribe({
      next: (raw) => {
        this.loading = false;
        const item = (raw?.body ?? null) as Skill | null;
        if (raw?.code === 200 && item) {
          this.applyToForm(item);
        } else {
          this.notifyFromResponse(raw, this.t('skill.messages.loadFailed'));
          this.formVisible = false;
        }
      },
      error: () => {
        this.loading = false;
        this.formVisible = false;
      },
    });
  }

  onDelete(row: Skill): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning(this.t('common.error'), this.t('skill.messages.missingId'));
      return;
    }
    const label = row.code?.trim() || row.name?.trim() || id;
    this.modal.confirm({
      nzTitle: this.t('skill.messages.confirmDeleteTitle'),
      nzContent: this.t('skill.messages.confirmDeleteContent').replace('{{label}}', label),
      nzOkText: this.t('common.button.delete'),
      nzOkDanger: true,
      nzCancelText: this.t('common.button.cancel'),
      nzOnOk: () =>
        new Promise<void>((resolve, reject) => {
          this.service.delete(id).subscribe({
            next: (raw) => {
              if (raw?.code === 200) {
                this.notification.success(this.t('common.button.done'), raw?.message ?? this.t('skill.messages.deleted'));
                this.fetch();
                resolve();
              } else {
                this.notifyFromResponse(raw, this.t('skill.messages.deleteFailed'));
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
    const payload: SkillWritePayload = {
      code: v.code.trim(),
      name: v.name.trim(),
      category: v.category?.trim() ? v.category.trim() : null,
      description: v.description?.trim() ? v.description.trim() : null,
    };

    if (this.formMode === 'create') {
      this.submitting = true;
      this.service.create(payload).subscribe({
        next: (raw) => {
          this.submitting = false;
          if (raw?.code === 201) {
            this.notification.success(this.t('common.button.done'), raw?.message ?? this.t('skill.messages.created'));
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
      this.notification.warning(this.t('common.error'), this.t('skill.messages.missingId'));
      return;
    }
    this.submitting = true;
    this.service.update(id, payload).subscribe({
      next: (raw) => {
        this.submitting = false;
        if (raw?.code === 200) {
          this.notification.success(this.t('common.button.done'), raw?.message ?? this.t('skill.messages.updated'));
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
    const req: SkillSearchRequest = {
      keyword: this.keyword?.trim() || null,
      category: this.category?.trim() || null,
      pageIndex: Math.max(0, (this.pageIndex ?? 1) - 1),
      pageSize: this.pageSize,
      voided: false,
    };
    this.service.getPage(req).subscribe((res) => {
      const page = (res?.body ?? null) as Page<Skill> | null;
      this.page = page;
      this.rows = page?.content ?? [];
      this.pageSize = page?.size ?? this.pageSize;
      this.pageIndex = (page?.number ?? req.pageIndex ?? 0) + 1;
    });
  }

  private resetModalForm(): void {
    this.modalForm.reset({ code: '', name: '', category: '', description: '' });
  }

  private applyToForm(item: Skill): void {
    this.modalForm.patchValue({
      code: item.code ?? '',
      name: item.name ?? '',
      category: (item.category ?? '') as string,
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
    this.notification.warning(this.t('common.error'), raw?.message ?? this.t('skill.messages.actionFailed'));
  }

  private notifyFromResponse(raw: ApiResponse | undefined, fallback: string): void {
    this.notification.warning(this.t('common.error'), raw?.message ?? fallback);
  }

  private t(key: string): string {
    return this.translate.instant(key);
  }
}

