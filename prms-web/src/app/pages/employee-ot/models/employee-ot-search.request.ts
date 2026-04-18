import { EmployeeOtStatus, EmployeeOtType } from './employee-ot.types';

export interface EmployeeOtSearchRequest {
  keyword?: string | null;
  userId?: string | null;
  projectId?: string | null;
  status?: EmployeeOtStatus | null;
  otType?: EmployeeOtType | null;
  otDateFrom?: Date | null;
  otDateTo?: Date | null;
  pageIndex?: number;
  pageSize?: number;
  voided?: boolean | null;
}
