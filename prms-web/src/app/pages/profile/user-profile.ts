import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { NzButtonComponent } from 'ng-zorro-antd/button';
import { NzCardComponent } from 'ng-zorro-antd/card';
import { NzColDirective, NzRowDirective } from 'ng-zorro-antd/grid';
import { NzFormDirective } from 'ng-zorro-antd/form';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { NzSpinComponent } from 'ng-zorro-antd/spin';
import { InputCommon } from '../../shared/input/input';
import { StoreService } from '../../core/services/store-service';
import { UserService } from '../management/users/services/user.service';
import { ApiResponse } from '../../shared/utils/api-response';
import { API_CODE_BAD_REQUEST, API_CODE_SUCCESS, GUTTER_H, GUTTER_V } from '../../shared/utils/const';
import { clearServerErrorsOnFormGroup, SERVER_FORM_ERROR_KEY } from '../../shared/utils/form-server-errors';
import {
  markFormControlsTouched,
  matchValueValidator,
  trimRequiredValidator,
} from '../../shared/utils/form-validation';
import { CurrentUserProfilePayload, User } from '../management/users/models/user.model';

@Component({
  selector: 'app-user-profile',
  imports: [
    ReactiveFormsModule,
    TranslatePipe,
    NzRowDirective,
    NzColDirective,
    NzFormDirective,
    NzCardComponent,
    NzButtonComponent,
    NzSpinComponent,
    InputCommon,
  ],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.scss',
})
export class UserProfile implements OnInit {
  readonly profileForm = new FormGroup({
    email: new FormControl<string>('', [Validators.required, trimRequiredValidator, Validators.email]),
    fullName: new FormControl<string>('', [Validators.required, trimRequiredValidator]),
  });

  /** Hiển thị only — không gửi khi cập nhật profile */
  readonlyUsername = '';

  readonly passwordForm = new FormGroup({
    currentPassword: new FormControl<string>('', [Validators.required]),
    newPassword: new FormControl<string>('', [Validators.required, Validators.minLength(6)]),
    confirmNewPassword: new FormControl<string>('', [Validators.required,matchValueValidator('newPassword')]),
  });

  pageLoading = false;
  profileSubmitting = false;
  passwordSubmitting = false;

  protected readonly GUTTER_H = GUTTER_H;
  protected readonly GUTTER_V = GUTTER_V;

  constructor(
    private userService: UserService,
    private store: StoreService,
    private notification: NzNotificationService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.pageLoading = true;
    clearServerErrorsOnFormGroup(this.profileForm);
    this.userService.getCurrent().subscribe({
      next: (res) => {
        this.pageLoading = false;
        const user = (res?.body ?? null) as User | null;
        if (res?.code === API_CODE_SUCCESS && user) {
          this.readonlyUsername = user.username ?? '';
          this.profileForm.patchValue({
            email: user.email ?? '',
            fullName: user.fullName ?? '',
          });
          return;
        }
        this.notification.warning(
          this.translate.instant('common.error'),
          this.translate.instant('profile.loadError')
        );
      },
      error: () => {
        this.pageLoading = false;
      },
    });
  }

  saveProfile(): void {
    clearServerErrorsOnFormGroup(this.profileForm);
    if (this.profileForm.invalid) {
      markFormControlsTouched(this.profileForm);
      return;
    }
    const v = this.profileForm.getRawValue();
    const payload: CurrentUserProfilePayload = {
      email: String(v.email ?? '').trim(),
      fullName: String(v.fullName ?? '').trim(),
    };
    this.profileSubmitting = true;
    this.userService.updateCurrentProfile(payload).subscribe({
      next: (raw) => {
        this.profileSubmitting = false;
        if (raw?.code === API_CODE_SUCCESS) {
          this.notification.success(
            this.translate.instant('common.button.done'),
            raw?.message ?? this.translate.instant('profile.saveProfileSuccess')
          );
          this.refreshStoreFromServer();
          return;
        }
        if (raw?.code === API_CODE_BAD_REQUEST) {
          if (raw.body && typeof raw.body === 'object' && !Array.isArray(raw.body)) {
            const body = raw.body as Record<string, unknown>;
            Object.keys(body).forEach((key) => {
              const msg = body[key];
              if (typeof msg !== 'string' || !msg.trim()) {
                return;
              }
              this.profileForm.controls[key]?.setErrors({ [SERVER_FORM_ERROR_KEY]: msg });
            });
          }
          return;
        }
        this.notification.warning(
          this.translate.instant('common.error'),
          raw?.message ?? this.translate.instant('common.commonError')
        );
      },
      error: () => {
        this.profileSubmitting = false;
      },
    });
  }

  savePassword(): void {
    clearServerErrorsOnFormGroup(this.passwordForm);
    if (this.passwordForm.invalid) {
      markFormControlsTouched(this.passwordForm);
      return;
    }
    const f = this.passwordForm.getRawValue();
    if (String(f.newPassword ?? '') !== String(f.confirmNewPassword ?? '')) {
      this.passwordForm.get('confirmNewPassword')?.setErrors({
        [SERVER_FORM_ERROR_KEY]: this.translate.instant('profile.passwordMismatch'),
      });
      markFormControlsTouched(this.passwordForm);
      return;
    }
    this.passwordSubmitting = true;
    this.userService.updateCurrentPassword(String(f.currentPassword ?? ''), String(f.newPassword ?? '')).subscribe({
      next: (raw) => {
        this.passwordSubmitting = false;
        if (raw?.code === API_CODE_SUCCESS) {
          this.notification.success(
            this.translate.instant('common.button.done'),
            raw?.message ?? this.translate.instant('profile.savePasswordSuccess')
          );
          this.passwordForm.reset({
            currentPassword: '',
            newPassword: '',
            confirmNewPassword: '',
          });
          clearServerErrorsOnFormGroup(this.passwordForm);
          return;
        }
        if (raw?.code === API_CODE_BAD_REQUEST) {
          if (raw.body && typeof raw.body === 'object' && !Array.isArray(raw.body)) {
            const body = raw.body as Record<string, unknown>;
            Object.keys(body).forEach((key) => {
              const msg = body[key];
              if (typeof msg !== 'string' || !msg.trim()) {
                return;
              }
              this.passwordForm.controls[key]?.setErrors({ [SERVER_FORM_ERROR_KEY]: msg });
            });
          }
          return;
        }
        this.notification.warning(
          this.translate.instant('common.error'),
          raw?.message ?? this.translate.instant('common.commonError')
        );
      },
      error: () => {
        this.passwordSubmitting = false;
      },
    });
  }

  private refreshStoreFromServer(): void {
    this.store.getLoginUser().subscribe({
      next: (value: ApiResponse) => {
        if (value?.code === API_CODE_SUCCESS && value.body) {
          this.store.setCurrentUser(value.body);
        }
      },
      error: () => {},
    });
  }
}
