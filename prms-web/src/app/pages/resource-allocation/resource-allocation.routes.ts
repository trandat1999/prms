import { Routes } from '@angular/router';
import { ResourceAllocationList } from './resource-allocation-list/resource-allocation-list';

export const RESOURCE_ALLOCATION_ROUTES: Routes = [
  {
    path: '',
    component: ResourceAllocationList,
    data: { breadcrumb: 'breadcrumb.resourceAllocation' },
  },
];
