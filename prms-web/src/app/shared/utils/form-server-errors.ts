import { AbstractControl, FormGroup } from '@angular/forms';
import { ApiResponse } from './api-response';
import { API_CODE_BAD_REQUEST } from './const';

export const SERVER_FORM_ERROR_KEY = 'serverError';

/** Map field → message khi BE trả `code === 400` và `body` là object các chuỗi lỗi. */
export function parseServerFieldErrorMap(raw: ApiResponse | undefined): Record<string, string> | null {
  if (raw?.code !== API_CODE_BAD_REQUEST || raw.body == null) {
    return null;
  }
  const body = raw.body;
  if (typeof body !== 'object' || Array.isArray(body)) {
    return null;
  }
  const out: Record<string, string> = {};
  for (const [k, v] of Object.entries(body as Record<string, unknown>)) {
    if (typeof v === 'string' && v.trim()) {
      out[k] = v;
    }
  }
  return Object.keys(out).length > 0 ? out : null;
}

function stripServerError(control: AbstractControl): void {
  const err = control.errors;
  if (!err || !(SERVER_FORM_ERROR_KEY in err)) {
    return;
  }
  const next = { ...err };
  delete next[SERVER_FORM_ERROR_KEY];
  control.setErrors(Object.keys(next).length ? next : null);
}

/** Gỡ `serverError` khỏi mọi control (giữ validator khác). */
export function clearServerErrorsOnFormGroup(form: FormGroup): void {
  for (const c of Object.values(form.controls)) {
    stripServerError(c);
  }
}

/** Gán lỗi BE vào `FormControl` theo key trùng tên field (merge với errors hiện có). */
export function applyServerFieldErrorsToFormGroup(form: FormGroup, map: Record<string, string>): void {
  for (const [key, message] of Object.entries(map)) {
    const c = form.controls[key];
    if (!c) {
      continue;
    }
    const existing = { ...(c.errors ?? {}) };
    existing[SERVER_FORM_ERROR_KEY] = message;
    c.setErrors(existing);
  }
}
