import { Routes } from '@angular/router';
import { EmployeeOtList } from './employee-ot-list/employee-ot-list';

export const EMPLOYEE_OT_ROUTES: Routes = [
  {
    path: '',
    component: EmployeeOtList,
    data: { breadcrumb: 'breadcrumb.employeeOt' },
  },
];
