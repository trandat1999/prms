export interface DashboardSummary {
  totalPyc: number;
  activePyc: number;
  totalTask: number;
  taskDone: number;
  otHoursMonth: number;
  resourceUsagePercent: number;
}

export interface DashboardMonthUtilization {
  month: string;
  utilizationPercent: number;
}

export interface DashboardTaskStatusCount {
  status: string;
  count: number;
}

export interface DashboardLabeledPercent {
  label: string;
  percent: number;
}

export interface DashboardLabeledHours {
  label: string;
  hours: number;
}

export interface DashboardPycProgress {
  code: string;
  name: string;
  progressPercent: number;
}

export interface DashboardOverview {
  summary: DashboardSummary;
  resourceUtilizationByMonth: DashboardMonthUtilization[];
  taskStatusDistribution: DashboardTaskStatusCount[];
  topPycByResource: DashboardLabeledPercent[];
  otHoursByProject: DashboardLabeledHours[];
  employeeWorkload: DashboardLabeledPercent[];
  pycProgress: DashboardPycProgress[];
}
