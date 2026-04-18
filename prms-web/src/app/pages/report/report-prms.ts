import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import type { EChartsCoreOption } from 'echarts/core';
import * as echarts from 'echarts/core';
import { BarChart, LineChart } from 'echarts/charts';
import {
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { NgxEchartsDirective, provideEchartsCore } from 'ngx-echarts';
import { NzButtonComponent } from 'ng-zorro-antd/button';
import { NzCardComponent } from 'ng-zorro-antd/card';
import { NzDividerComponent } from 'ng-zorro-antd/divider';
import { NzEmptyComponent } from 'ng-zorro-antd/empty';
import { NzTableModule } from 'ng-zorro-antd/table';
import { InputCommon } from '../../shared/input/input';
import { ReportData, ReportPeriodType } from './models/report.model';
import { take } from 'rxjs';
import { ReportService } from './services/report.service';

echarts.use([
  BarChart,
  LineChart,
  GridComponent,
  LegendComponent,
  TooltipComponent,
  TitleComponent,
  CanvasRenderer,
]);

@Component({
  selector: 'app-report-prms',
  imports: [
    CommonModule,
    FormsModule,
    TranslatePipe,
    NgxEchartsDirective,
    NzCardComponent,
    NzTableModule,
    NzDividerComponent,
    NzEmptyComponent,
    NzButtonComponent,
    InputCommon,
  ],
  templateUrl: './report-prms.html',
  styleUrl: './report-prms.scss',
  providers: [provideEchartsCore({ echarts })],
})
export class ReportPrms implements OnInit {
  private readonly reportService = inject(ReportService);
  private readonly translate = inject(TranslateService);

  readonly periodItems = [
    { label: 'report.period.month', value: 'MONTH' as ReportPeriodType },
    { label: 'report.period.quarter', value: 'QUARTER' as ReportPeriodType },
    { label: 'report.period.year', value: 'YEAR' as ReportPeriodType },
  ];

  filter: { periodType: ReportPeriodType; year: number } = {
    periodType: 'MONTH',
    year: new Date().getFullYear(),
  };
  yearDate: Date = new Date(new Date().getFullYear(), 0, 1);

  data: ReportData | null = null;
  loadError = false;

  chartCostRevenue: EChartsCoreOption = {};
  chartAllocationStack: EChartsCoreOption = {};
  chartOt: EChartsCoreOption = {};
  chartTaskTrend: EChartsCoreOption = {};

  ngOnInit(): void {
    this.reload();
  }

  onYearDateChange(d: Date | null): void {
    if (d) {
      this.yearDate = d;
      this.filter = { ...this.filter, year: d.getFullYear() };
    }
  }

  onPeriodChange(v: ReportPeriodType): void {
    this.filter = { ...this.filter, periodType: v };
  }

  reload(): void {
    this.reportService
      .loadReport({ periodType: this.filter.periodType, year: this.filter.year })
      .pipe(take(1))
      .subscribe({
        next: ({ data }) => {
          this.loadError = false;
          this.data = data;
          if (data) {
            this.chartCostRevenue = this.buildCostRevenueChart(data);
            this.chartAllocationStack = this.buildAllocationStacked(data);
            this.chartOt = this.buildOtBar(data);
            this.chartTaskTrend = this.buildTaskTrend(data);
          }
        },
        error: () => {
          this.loadError = true;
        },
      });
  }

  /** Định dạng số MM (triệu / đơn vị báo cáo) */
  fmtMm(n: number | null | undefined): string {
    const v = Number(n ?? 0);
    return (
      new Intl.NumberFormat(this.translate.currentLang?.startsWith('vi') ? 'vi-VN' : 'en-US', {
        maximumFractionDigits: 2,
        minimumFractionDigits: 0,
      }).format(v) + ' MM'
    );
  }

  private buildCostRevenueChart(d: ReportData): EChartsCoreOption {
    const xs = d.costVsRevenue.map((r) => r.periodLabel);
    const costs = d.costVsRevenue.map((r) => Number(r.resourceCost ?? 0));
    const revs = d.costVsRevenue.map((r) => Number(r.pycRevenue ?? 0));
    const profits = d.costVsRevenue.map((r) => Number(r.profit ?? 0));
    return {
      title: {
        text: this.translate.instant('report.charts.costVsRevenue'),
        left: 0,
        textStyle: { fontSize: 14 },
      },
      tooltip: {
        trigger: 'axis',
        formatter: (params: unknown) => {
          const arr = Array.isArray(params) ? params : [params];
          const first = arr[0] as { axisValue?: string } | undefined;
          const lines = [first?.axisValue ?? ''];
          for (const p of arr) {
            const row = p as { seriesName?: string; value?: number };
            lines.push(`${row.seriesName ?? ''}: ${this.fmtMm(row.value)}`);
          }
          return lines.join('<br/>');
        },
      },
      legend: { top: 28 },
      grid: { left: 72, right: 16, top: 72, bottom: 48 },
      xAxis: { type: 'category', data: xs, axisLabel: { rotate: xs.length > 6 ? 30 : 0 } },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (v: number) =>
            new Intl.NumberFormat(this.translate.currentLang?.startsWith('vi') ? 'vi-VN' : 'en-US', {
              maximumFractionDigits: 1,
            }).format(v),
        },
      },
      series: [
        {
          name: this.translate.instant('report.table.resourceCost'),
          type: 'bar',
          data: costs,
        },
        {
          name: this.translate.instant('report.table.pycRevenue'),
          type: 'bar',
          data: revs,
        },
        {
          name: this.translate.instant('report.table.profit'),
          type: 'bar',
          data: profits.map((v) => ({
            value: v,
            itemStyle: v < 0 ? { color: '#cf1322' } : undefined,
          })),
        },
      ],
    };
  }

  private buildAllocationStacked(d: ReportData): EChartsCoreOption {
    const st = d.allocationStacked;
    if (!st?.periodLabels?.length || !st.series?.length) {
      return {
        title: {
          text:
            this.translate.instant('report.charts.allocationStacked') +
            ' — ' +
            this.translate.instant('report.empty.allocation'),
          left: 0,
          textStyle: { fontSize: 14, color: '#999' },
        },
      };
    }
    const series = st.series.map((s) => ({
      name: s.userLabel,
      type: 'bar' as const,
      stack: 'alloc',
      emphasis: { focus: 'series' as const },
      data: s.values.map((v) => Number(v ?? 0)),
    }));
    return {
      title: {
        text: this.translate.instant('report.charts.allocationStacked'),
        left: 0,
        textStyle: { fontSize: 14 },
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' as const },
        formatter: (params: unknown) => {
          const arr = Array.isArray(params) ? params : [params];
          const first = arr[0] as { axisValue?: string } | undefined;
          const lines = [first?.axisValue ?? ''];
          for (const p of arr) {
            const row = p as { seriesName?: string; value?: number };
            lines.push(`${row.seriesName ?? ''}: ${this.fmtMm(row.value ?? 0)}`);
          }
          return lines.join('<br/>');
        },
      },
      legend: { type: 'scroll', top: 28, left: 'center' },
      grid: { left: 48, right: 16, top: 80, bottom: 48 },
      xAxis: { type: 'category', data: st.periodLabels, axisLabel: { rotate: 30 } },
      yAxis: { type: 'value', name: 'MM' },
      series,
    };
  }

  private buildOtBar(d: ReportData): EChartsCoreOption {
    const rows = [...d.otByUserMonth]
      .sort((a, b) => Number(b.otHours) - Number(a.otHours))
      .slice(0, 24);
    const xs = rows.map((r) => `${r.userLabel} / ${r.month}`);
    const ys = rows.map((r) => Number(r.otHours ?? 0));
    return {
      title: {
        text: this.translate.instant('report.charts.otReport'),
        left: 0,
        textStyle: { fontSize: 14 },
      },
      tooltip: {
        trigger: 'axis',
        formatter: (params: unknown) => {
          const p = Array.isArray(params) ? params[0] : params;
          if (!p || typeof p !== 'object') {
            return '';
          }
          const row = p as { axisValue?: string; name?: string; value?: number };
          const label = row.axisValue ?? row.name ?? '';
          return `${label}<br/>${row.value ?? 0} h`;
        },
      },
      grid: { left: 56, right: 16, top: 48, bottom: 120 },
      xAxis: { type: 'category', data: xs, axisLabel: { rotate: 45, fontSize: 10 } },
      yAxis: { type: 'value', name: 'h' },
      series: [{ type: 'bar', data: ys }],
    };
  }

  private buildTaskTrend(d: ReportData): EChartsCoreOption {
    const xs = d.taskCompletionTrend.map((r) => r.weekLabel);
    const ys = d.taskCompletionTrend.map((r) => Number(r.tasksDone ?? 0));
    return {
      title: {
        text: this.translate.instant('report.charts.taskTrend'),
        left: 0,
        textStyle: { fontSize: 14 },
      },
      tooltip: { trigger: 'axis' },
      grid: { left: 48, right: 16, top: 48, bottom: 40 },
      xAxis: { type: 'category', data: xs, boundaryGap: false },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          type: 'line',
          smooth: true,
          name: this.translate.instant('report.table.tasksDone'),
          data: ys,
          areaStyle: { opacity: 0.06 },
        },
      ],
    };
  }
}
