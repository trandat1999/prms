import { AbstractControl, FormArray, FormGroup, ValidationErrors, ValidatorFn } from '@angular/forms';

export const DATE_RANGE_ERROR_KEY = 'dateRange';
export const TIME_RANGE_ERROR_KEY = 'timeRange';
export const CHECKLIST_TITLE_REQUIRED_ERROR_KEY = 'checklistTitleRequired';

export function trimRequiredValidator(control: AbstractControl): ValidationErrors | null {
  const value = control.value;
  if (value == null) {
    return { required: true };
  }
  if (typeof value === 'string' && value.trim().length === 0) {
    return { required: true };
  }
  if (Array.isArray(value) && value.length === 0) {
    return { required: true };
  }
  return null;
}

export function siblingDateRangeValidator(
  siblingControlName: string,
  errorKey = DATE_RANGE_ERROR_KEY
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const sibling = control.parent?.get(siblingControlName);
    const start = sibling?.value;
    const end = control.value;
    if (!start || !end) {
      return null;
    }
    return new Date(start).getTime() > new Date(end).getTime() ? { [errorKey]: true } : null;
  };
}

export function markFormControlsTouched(control: AbstractControl): void {
  control.markAllAsTouched();
  markControlTreeDirty(control);
  updateControlTreeValidity(control);
}

export function isInvalidAndTouched(control: AbstractControl | null | undefined): boolean {
  return !!control?.invalid && (control.touched || control.dirty);
}

function markControlTreeDirty(control: AbstractControl): void {
  control.markAsDirty();
  if (control instanceof FormGroup || control instanceof FormArray) {
    Object.values(control.controls).forEach((child) => markControlTreeDirty(child));
  }
}

function updateControlTreeValidity(control: AbstractControl): void {
  if (control instanceof FormGroup || control instanceof FormArray) {
    Object.values(control.controls).forEach((child) => updateControlTreeValidity(child));
  }
  control.updateValueAndValidity({ onlySelf: true, emitEvent: true });
}
