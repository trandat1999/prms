export interface NavigationItem {
  translateKey?: string;
  name?: string;
  link?: string;
  iconType?: string;
  iconClass?: string;
  iconLink?: string;
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
    children: [
      {
        translateKey: 'navigation.users',
        link: '/management/users',
      },
      {
        translateKey: 'navigation.appParams',
        link: '/management/app-params',
      },
    ],
  }
]
