export enum EmployeeOtStatus {
  DRAFT = 'DRAFT',
  SUBMITTED = 'SUBMITTED',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

export enum EmployeeOtType {
  WEEKDAY = 'WEEKDAY',
  WEEKEND = 'WEEKEND',
  HOLIDAY = 'HOLIDAY',
}

/** Hệ số OT — khớp với `EmployeeOtTypeEnum` backend */
export const EMPLOYEE_OT_TYPE_COEFFICIENT: Record<EmployeeOtType, number> = {
  [EmployeeOtType.WEEKDAY]: 1.5,
  [EmployeeOtType.WEEKEND]: 2.0,
  [EmployeeOtType.HOLIDAY]: 3.0,
};
