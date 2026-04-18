import { EmployeeOtStatus, EmployeeOtType } from './employee-ot.types';

export interface EmployeeOt {
  id?: string;
  userId: string;
  userDisplay?: string | null;
  projectId?: string | null;
  projectDisplay?: string | null;
  otDate: string | Date;
  startTime?: string | Date | null;
  endTime?: string | Date | null;
  otHours?: number | null;
  otType: EmployeeOtType;
  otTypeCoefficient?: number | null;
  weightedOtHours?: number | null;
  reason?: string | null;
  status?: EmployeeOtStatus | null;
  approvedBy?: string | null;
  approvedByDisplay?: string | null;
  approvedDate?: string | Date | null;
}

export type EmployeeOtWritePayload = Pick<
  EmployeeOt,
  | 'userId'
  | 'projectId'
  | 'otDate'
  | 'startTime'
  | 'endTime'
  | 'otHours'
  | 'otType'
  | 'reason'
  | 'status'
>;
