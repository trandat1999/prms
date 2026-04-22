import { CommonModule, DatePipe } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NzButtonComponent } from 'ng-zorro-antd/button';
import { NzIconDirective } from 'ng-zorro-antd/icon';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { NzSpinComponent } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagComponent } from 'ng-zorro-antd/tag';
import { TranslatePipe } from '@ngx-translate/core';
import { InputCommon } from '../../../../shared/input/input';
import { ApiResponse } from '../../../../shared/utils/api-response';
import {
  applyServerFieldErrorsToFormGroup,
  clearServerErrorsOnFormGroup,
  parseServerFieldErrorMap,
} from '../../../../shared/utils/form-server-errors';
import { Page } from '../../../project/models/page.model';
import { Role } from '../models/role.model';
import { User, UserCreatePayload, UserDetail, UserUpdatePayload } from '../models/user.model';
import { UserSearchRequest } from '../models/user-search.request';
import { RoleService } from '../services/role.service';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-user-list',
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
  templateUrl: './user-list.html',
  styleUrls: ['./user-list.scss'],
})
export class UserList {
  keyword = '';
  enabled: boolean | null = null;

  rows: User[] = [];
  page: Page<User> | null = null;

  pageIndex = 1; // nz-table là 1-based
  pageSize = 20;

  /** Modal thêm / sửa */
  userFormVisible = false;
  userFormMode: 'create' | 'edit' = 'create';
  editingUserId: string | null = null;
  userFormSubmitting = false;
  userFormLoading = false;
  readonly userModalForm = new FormGroup({
    username: new FormControl<string>(''),
    password: new FormControl<string>(''),
    email: new FormControl<string>(''),
    fullName: new FormControl<string>(''),
    enabled: new FormControl<boolean>(true),
    roleCodes: new FormControl<string[]>(['USER']),
  });
  readonly passwordModalForm = new FormGroup({
    newPassword: new FormControl<string>(''),
  });

  /** Modal xem */
  viewVisible = false;
  viewLoading = false;
  viewDetail: UserDetail | null = null;

  /** Roles từ BE */
  roleOptions: Role[] = [];

  /** Modal cập nhật mật khẩu */
  passwordVisible = false;
  passwordSubmitting = false;
  passwordUserId: string | null = null;
  passwordUserLabel: string | null = null;

  readonly enabledOptions = [
    { label: 'Tất cả', value: null },
    { label: 'Đang hoạt động', value: true },
    { label: 'Bị khoá / tắt', value: false },
  ];

  constructor(
    private userService: UserService,
    private roleService: RoleService,
    private notification: NzNotificationService,
    private modal: NzModalService
  ) {}

  ngOnInit(): void {
    this.fetchRoles();
    this.fetch();
  }

  get userFormTitle(): string {
    return this.userFormMode === 'create' ? 'Thêm người dùng' : 'Sửa người dùng';
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
    this.userFormMode = 'create';
    this.editingUserId = null;
    this.userFormLoading = false;
    this.resetUserModalForm();
    this.ensureDefaultRoles();
    clearServerErrorsOnFormGroup(this.userModalForm);
    this.userFormVisible = true;
  }

  onEdit(row: User): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning('Lỗi', 'Không có mã người dùng để sửa.');
      return;
    }
    this.userFormMode = 'edit';
    this.editingUserId = id;
    this.userFormVisible = true;
    this.userFormLoading = true;
    this.resetUserModalForm();
    clearServerErrorsOnFormGroup(this.userModalForm);

    this.userService.getById(id).subscribe({
      next: ({ raw, user }) => {
        this.userFormLoading = false;
        if (raw?.code === 200 && user) {
          this.applyUserToForm(user);
          this.ensureDefaultRoles();
        } else {
          this.notifyFromResponse(raw, 'Không tải được người dùng.');
          this.userFormVisible = false;
        }
      },
      error: () => {
        this.userFormLoading = false;
        this.userFormVisible = false;
      },
    });
  }

  onView(row: User): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning('Lỗi', 'Không có mã người dùng để xem.');
      return;
    }
    this.viewVisible = true;
    this.viewDetail = null;
    this.viewLoading = true;
    this.userService.getById(id).subscribe({
      next: ({ raw, user }) => {
        this.viewLoading = false;
        if (raw?.code === 200 && user) {
          this.viewDetail = user;
        } else {
          this.notifyFromResponse(raw, 'Không tải được chi tiết người dùng.');
          this.viewVisible = false;
        }
      },
      error: () => {
        this.viewLoading = false;
        this.viewVisible = false;
      },
    });
  }

  onDelete(row: User): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning('Lỗi', 'Không có mã người dùng để xoá.');
      return;
    }
    const label = row.fullName?.trim() || row.username || id;
    this.modal.confirm({
      nzTitle: 'Xác nhận xoá',
      nzContent: `Bạn có chắc muốn xoá "${label}"? (soft delete)`,
      nzOkText: 'Xoá',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzCancelText: 'Huỷ',
      nzOnOk: () =>
        new Promise<void>((resolve, reject) => {
          this.userService.delete(id).subscribe({
            next: ({ raw }) => {
              if (raw?.code === 200) {
                this.notification.success('Đã xoá', raw?.message ?? 'Xoá thành công.');
                this.fetch();
                resolve();
              } else {
                this.notifyFromResponse(raw, 'Không xoá được người dùng.');
                reject();
              }
            },
            error: () => reject(),
          });
        }),
    });
  }

  closeUserFormModal(): void {
    this.userFormVisible = false;
    clearServerErrorsOnFormGroup(this.userModalForm);
  }

  closeViewModal(): void {
    this.viewVisible = false;
    this.viewDetail = null;
  }

  onChangePassword(row: User): void {
    const id = row?.id;
    if (!id) {
      this.notification.warning('Lỗi', 'Không có mã người dùng để đổi mật khẩu.');
      return;
    }
    this.passwordUserId = id;
    this.passwordUserLabel = row.fullName?.trim() || row.username || id;
    this.passwordModalForm.reset({ newPassword: '' });
    clearServerErrorsOnFormGroup(this.passwordModalForm);
    this.passwordVisible = true;
  }

  closePasswordModal(): void {
    this.passwordVisible = false;
    this.passwordUserId = null;
    this.passwordUserLabel = null;
    clearServerErrorsOnFormGroup(this.passwordModalForm);
  }

  savePassword(): void {
    const id = this.passwordUserId;
    const pwd = String(this.passwordModalForm.get('newPassword')?.value ?? '');
    if (!id) return;
    clearServerErrorsOnFormGroup(this.passwordModalForm);
    if (!pwd || String(pwd).length < 6) {
      this.notification.warning('Thiếu dữ liệu', 'Mật khẩu tối thiểu 6 ký tự.');
      return;
    }
    this.passwordSubmitting = true;
    this.userService.updatePassword(id, pwd).subscribe({
      next: ({ raw }) => {
        this.passwordSubmitting = false;
        if (raw?.code === 200) {
          this.notification.success('Thành công', raw?.message ?? 'Đã cập nhật mật khẩu.');
          this.closePasswordModal();
          return;
        }
        this.handleWriteError(raw, 'password');
      },
      error: () => {
        this.passwordSubmitting = false;
      },
    });
  }

  saveUserForm(): void {
    if (this.userFormMode === 'edit' && this.userFormLoading) return;
    clearServerErrorsOnFormGroup(this.userModalForm);
    if (!this.validateForm()) return;

    if (this.userFormMode === 'create') {
      this.submitCreate();
      return;
    }
    const id = this.editingUserId;
    if (!id) {
      this.notification.warning('Lỗi', 'Thiếu mã người dùng khi cập nhật.');
      return;
    }
    this.submitUpdate(id);
  }

  enabledTagColor(v: boolean | undefined): string {
    return v ? 'green' : 'red';
  }

  enabledLabel(v: boolean | undefined): string {
    return v ? 'Đang hoạt động' : 'Bị khoá';
  }

  private fetch(): void {
    const req: UserSearchRequest = {
      keyword: this.keyword?.trim() || null,
      enabled: this.enabled,
      pageIndex: Math.max(0, (this.pageIndex ?? 1) - 1),
      pageSize: this.pageSize,
      voided: false,
    };

    this.userService.getPage(req).subscribe(({ page }) => {
      this.page = page;
      this.rows = page?.content ?? [];
      const backendSize = page?.size ?? this.pageSize;
      const backendIndex0 = page?.number ?? req.pageIndex ?? 0;
      this.pageSize = backendSize;
      this.pageIndex = backendIndex0 + 1;
    });
  }

  private resetUserModalForm(): void {
    this.userModalForm.reset({
      username: '',
      password: '',
      email: '',
      fullName: '',
      enabled: true,
      roleCodes: ['USER'],
    });
  }

  private applyUserToForm(u: UserDetail): void {
    this.userModalForm.patchValue({
      username: u.username ?? '',
      password: '',
      email: u.email ?? '',
      fullName: u.fullName ?? '',
      enabled: u.enabled ?? true,
      roleCodes: (u.roles ?? []).length ? (u.roles ?? []) : ['USER'],
    });
  }

  private validateForm(): boolean {
    const f = this.userModalForm.getRawValue();
    if (!f.username?.trim()) {
      this.notification.warning('Thiếu dữ liệu', 'Vui lòng nhập username.');
      return false;
    }
    if (!f.email?.trim()) {
      this.notification.warning('Thiếu dữ liệu', 'Vui lòng nhập email.');
      return false;
    }
    if (!f.fullName?.trim()) {
      this.notification.warning('Thiếu dữ liệu', 'Vui lòng nhập họ tên.');
      return false;
    }
    if (this.userFormMode === 'create') {
      if (!f.password || String(f.password).length < 6) {
        this.notification.warning('Thiếu dữ liệu', 'Mật khẩu tối thiểu 6 ký tự.');
        return false;
      }
    }
    return true;
  }

  private submitCreate(): void {
    const f = this.userModalForm.getRawValue();
    const payload: UserCreatePayload = {
      username: f.username.trim(),
      password: String(f.password ?? ''),
      email: f.email.trim(),
      fullName: f.fullName.trim(),
      enabled: f.enabled,
      roleCodes: f.roleCodes ?? null,
    };
    this.userFormSubmitting = true;
    this.userService.create(payload).subscribe({
      next: ({ raw }) => {
        this.userFormSubmitting = false;
        if (raw?.code === 201) {
          this.notification.success('Thành công', raw?.message ?? 'Đã tạo người dùng.');
          clearServerErrorsOnFormGroup(this.userModalForm);
          this.userFormVisible = false;
          this.fetch();
          return;
        }
        this.handleWriteError(raw);
      },
      error: () => {
        this.userFormSubmitting = false;
      },
    });
  }

  private submitUpdate(id: string): void {
    const f = this.userModalForm.getRawValue();
    const payload: UserUpdatePayload = {
      username: f.username.trim(),
      email: f.email.trim(),
      fullName: f.fullName.trim(),
      enabled: f.enabled,
      roleCodes: f.roleCodes ?? null,
    };
    this.userFormSubmitting = true;
    this.userService.update(id, payload).subscribe({
      next: ({ raw }) => {
        this.userFormSubmitting = false;
        if (raw?.code === 200) {
          this.notification.success('Thành công', raw?.message ?? 'Đã cập nhật người dùng.');
          clearServerErrorsOnFormGroup(this.userModalForm);
          this.userFormVisible = false;
          this.fetch();
          return;
        }
        this.handleWriteError(raw);
      },
      error: () => {
        this.userFormSubmitting = false;
      },
    });
  }

  private handleWriteError(raw: ApiResponse | undefined, target: 'user' | 'password' = 'user'): void {
    const map = parseServerFieldErrorMap(raw);
    if (map) {
      if (target === 'password') {
        applyServerFieldErrorsToFormGroup(this.passwordModalForm, map);
      } else {
        applyServerFieldErrorsToFormGroup(this.userModalForm, map);
      }
      return;
    }
    if (target === 'password') {
      clearServerErrorsOnFormGroup(this.passwordModalForm);
    } else {
      clearServerErrorsOnFormGroup(this.userModalForm);
    }
    this.notification.warning('Không thành công', raw?.message ?? 'Thao tác thất bại.');
  }

  private notifyFromResponse(raw: ApiResponse | undefined, fallback: string): void {
    this.notification.warning('Thông báo', raw?.message ?? fallback);
  }

  private fetchRoles(): void {
    this.roleService.getAll().subscribe({
      next: ({ raw, roles }) => {
        if (raw?.code === 200 && Array.isArray(roles)) {
          this.roleOptions = roles;
        }
      },
      error: () => {},
    });
  }

  private ensureDefaultRoles(): void {
    const codes = this.userModalForm.get('roleCodes')?.value;
    if (!codes || codes.length === 0) {
      this.userModalForm.patchValue({ roleCodes: ['USER'] });
    }
  }
}

