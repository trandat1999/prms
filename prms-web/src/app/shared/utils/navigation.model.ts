export interface NavigationItem {
  translateKey?: string;
  name?: string;
  link?: string;
  iconType?: string;
  iconClass?: string;
  iconLink?: string;
  allowedRoles?: string[];
  children?: NavigationItem[];
}

export const navigation: NavigationItem[] = [
  {
    translateKey: 'navigation.dashboard',
    iconType: 'dashboard',
    link: '/dashboard',
  },
  {
    translateKey: 'navigation.report',
    iconType: 'bar-chart',
    link: '/report',
    allowedRoles: ['SUPPER_ADMIN'],
  },
  {
    translateKey: 'navigation.projectRequest',
    iconType: "file-text",
    link: '/project',
  },
  {
    translateKey: 'navigation.resourceAllocation',
    iconType: 'team',
    link: '/resource-allocation',
    allowedRoles: ['SUPPER_ADMIN'],
  },
  {
    translateKey: 'navigation.employeeOt',
    iconType: 'clock-circle',
    link: '/employee-ot',
  },
  {
    translateKey: 'navigation.kanban',
    iconType: 'appstore',
    link: '/kanban',
  },
  {
    translateKey: 'navigation.management',
    iconType: 'setting',
    allowedRoles: ['SUPPER_ADMIN'],
    children: [
      {
        translateKey: 'navigation.users',
        link: '/management/users',
        allowedRoles: ['SUPPER_ADMIN'],
      },
      {
        translateKey: 'navigation.appParams',
        link: '/management/app-params',
        allowedRoles: ['SUPPER_ADMIN'],
      },
    ],
  }
]
