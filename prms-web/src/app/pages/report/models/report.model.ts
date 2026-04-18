export type ReportPeriodType = 'MONTH' | 'QUARTER' | 'YEAR';

export interface ReportFilter {
  periodType: ReportPeriodType;
  year: number;
}

export interface ReportCostRevenueRow {
  periodLabel: string;
  resourceCost: number;
  pycRevenue: number;
  profit: number;
}

export interface ReportStackSeries {
  userLabel: string;
  values: number[];
}

export interface ReportAllocationStacked {
  periodLabels: string[];
  series: ReportStackSeries[];
}

export interface ReportPersonnelPerformance {
  userLabel: string;
  tasksDone: number;
  otHours: number;
  laborMm: number;
}

export interface ReportUserOtMonth {
  userLabel: string;
  month: string;
  otHours: number;
}

export interface ReportProjectPerformance {
  projectCode: string;
  projectName: string;
  tasksDone: number;
  resourceSharePercent: number;
  otHours: number;
}

export interface ReportTaskWeekTrend {
  weekLabel: string;
  tasksDone: number;
}

export interface ReportData {
  periodType: ReportPeriodType;
  year: number;
  valueUnit: string;
  laborMmPerFullFteMonth: number;
  costVsRevenue: ReportCostRevenueRow[];
  allocationStacked: ReportAllocationStacked | null;
  personnelPerformance: ReportPersonnelPerformance[];
  otByUserMonth: ReportUserOtMonth[];
  projectPerformance: ReportProjectPerformance[];
  taskCompletionTrend: ReportTaskWeekTrend[];
}
