import { AbstractControl, FormGroup } from '@angular/forms';
import { ApiResponse } from './api-response';
import { API_CODE_BAD_REQUEST } from './const';
import { markFormControlsTouched } from './form-validation';

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

/** Lấy payload `ApiResponse` từ lỗi HTTP (sau `catchError` của BaseService). */
export function getApiResponseFromHttpError(err: unknown): ApiResponse | undefined {
  if (typeof err !== 'object' || err === null || !('error' in err)) {
    return undefined;
  }
  const e = (err as { error: unknown }).error;
  return e && typeof e === 'object' ? (e as ApiResponse) : undefined;
}

/**
 * 400: map field → control; nếu không parse được map thì gán một thông báo chung lên `fallbackControlKeys`.
 */
export function applyBadRequestResponseToFormGroup(
  form: FormGroup,
  raw: ApiResponse | undefined,
  opts?: { fallbackControlKeys?: string[] }
): void {
  const map = parseServerFieldErrorMap(raw);
  if (map) {
    applyServerFieldErrorsToFormGroup(form, map);
    markFormControlsTouched(form);
    return;
  }
  const msg =
    typeof raw?.body === 'string' && raw.body.trim()
      ? raw.body.trim()
      : typeof raw?.message === 'string' && raw.message.trim()
        ? raw.message.trim()
        : null;
  if (msg && opts?.fallbackControlKeys?.length) {
    for (const key of opts.fallbackControlKeys) {
      const c = form.controls[key];
      if (!c) {
        continue;
      }
      const existing = { ...(c.errors ?? {}) };
      existing[SERVER_FORM_ERROR_KEY] = msg;
      c.setErrors(existing);
    }
    markFormControlsTouched(form);
  }
}
