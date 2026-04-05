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
    iconType: "dashboard",
    children: [
      {
        name: 'Welcome',
        link: '/welcome'
      },
    ],
  },
  {
    translateKey: 'navigation.projectRequest',
    iconType: "file-text",
    link: '/project',
  }
]
