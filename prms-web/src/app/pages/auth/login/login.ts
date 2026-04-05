import {Component, OnInit} from '@angular/core';
import {API_CODE_BAD_REQUEST, GUTTER_H, GUTTER_V} from "../../../shared/utils/const";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../auth-service';
import {StorageService} from '../../../core/services/storage-service';
import {Router, RouterLink} from '@angular/router';
import {TranslatePipe} from '@ngx-translate/core';
import {NzColDirective, NzRowDirective} from 'ng-zorro-antd/grid';
import {NzFormDirective} from 'ng-zorro-antd/form';
import {InputCommon} from '../../../shared/input/input';
import {NzCheckboxComponent} from 'ng-zorro-antd/checkbox';
import {NzButtonComponent} from 'ng-zorro-antd/button';

@Component({
  selector: 'app-login',
  imports: [
    TranslatePipe,
    ReactiveFormsModule,
    NzRowDirective,
    NzFormDirective,
    InputCommon,
    NzColDirective,
    NzCheckboxComponent,
    RouterLink,
    NzButtonComponent
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login implements OnInit {
  formGroup: FormGroup
  isSubmitting = false;

  constructor(private authService: AuthService,
              private storageService: StorageService,
              private router: Router,) {
  }

  ngOnInit(): void {
    this.initForm();
  }

  initForm() {
    this.formGroup = new FormGroup({
      username: new FormControl('', [Validators.required]),
      password: new FormControl('', [Validators.required]),
      rememberMe: new FormControl(false),
    })
  }

  login() {
    this.isSubmitting = true;
    this.authService.login(this.formGroup.getRawValue()).subscribe((data) => {
      this.isSubmitting = false;
      if (data.code == API_CODE_BAD_REQUEST) {
        if (data.body) {
          Object.keys(data.body).forEach(key => {
            this.formGroup.controls[key]?.setErrors({'serverError': data.body[key]});
          });
          return;
        }
      } else {
        if (this.formGroup.get('rememberMe').value) {
          this.storageService.saveToken(data.body);
        } else {
          this.storageService.saveSessionToken(data.body);
        }
        this.router.navigate(['welcome']);
      }
    })
  }

  protected readonly GUTTER_H = GUTTER_H;
  protected readonly GUTTER_V = GUTTER_V;
}
