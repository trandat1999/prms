import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import type { EChartsCoreOption } from 'echarts/core';
import * as echarts from 'echarts/core';
import { BarChart, LineChart, PieChart } from 'echarts/charts';
import {
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { NgxEchartsDirective, provideEchartsCore } from 'ngx-echarts';
import { NzCardComponent } from 'ng-zorro-antd/card';
import { NzColDirective, NzRowDirective } from 'ng-zorro-antd/grid';
import { NzEmptyComponent } from 'ng-zorro-antd/empty';
import { NzProgressComponent } from 'ng-zorro-antd/progress';
import { NzStatisticComponent } from 'ng-zorro-antd/statistic';
import { DashboardOverview } from './models/dashboard.model';
import { DashboardService } from './services/dashboard.service';

echarts.use([
  LineChart,
  BarChart,
  PieChart,
  GridComponent,
  LegendComponent,
  TooltipComponent,
  TitleComponent,
  CanvasRenderer,
]);

@Component({
  selector: 'app-dashboard',
  imports: [
    CommonModule,
    TranslatePipe,
    NgxEchartsDirective,
    NzCardComponent,
    NzRowDirective,
    NzColDirective,
    NzStatisticComponent,
    NzEmptyComponent,
    NzProgressComponent,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  providers: [provideEchartsCore({ echarts })],
})
export class Dashboard implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly translate = inject(TranslateService);
  private readonly destroyRef = inject(DestroyRef);

  overview: DashboardOverview | null = null;
  loadError = false;

  chartResource: EChartsCoreOption = {};
  chartTaskPie: EChartsCoreOption = {};
  chartOtByProject: EChartsCoreOption = {};
  chartTopPyc: EChartsCoreOption = {};
  chartWorkload: EChartsCoreOption = {};

  ngOnInit(): void {
    this.dashboardService
      .getOverview()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ overview }) => {
          this.loadError = false;
          this.overview = overview;
          if (overview) {
            this.rebuildCharts(overview);
          }
        },
        error: () => {
          this.loadError = true;
        },
      });
  }

  private rebuildCharts(o: DashboardOverview): void {
    this.chartResource = this.buildResourceLine(o);
    this.chartTaskPie = this.buildTaskPie(o);
    this.chartOtByProject = this.buildProjectHoursBar(
      o.otHoursByProject,
      this.translate.instant('dashboard.charts.otByProjectY'),
    );
    this.chartTopPyc = this.buildPercentBar(o.topPycByResource, this.translate.instant('dashboard.charts.topPyc'));
    this.chartWorkload = this.buildWorkloadBar(o.employeeWorkload);
  }

  private monthLabel(ym: string): string {
    const parts = ym.split('-');
    if (parts.length !== 2) {
      return ym;
    }
    const y = Number(parts[0]);
    const m = Number(parts[1]);
    const d = new Date(y, m - 1, 1);
    const loc = this.translate.currentLang?.startsWith('vi') ? 'vi-VN' : 'en-US';
    return d.toLocaleDateString(loc, { month: 'short', year: 'numeric' });
  }

  private buildResourceLine(o: DashboardOverview): EChartsCoreOption {
    const xs = o.resourceUtilizationByMonth.map((r) => this.monthLabel(r.month));
    const ys = o.resourceUtilizationByMonth.map((r) => Number(r.utilizationPercent ?? 0));
    return {
      title: { text: this.translate.instant('dashboard.charts.resourceUtilization'), left: 0, textStyle: { fontSize: 14 } },
      tooltip: {
        trigger: 'axis',
        formatter: (params: unknown) => {
          const p = Array.isArray(params) ? params[0] : params;
          if (!p || typeof p !== 'object') {
            return '';
          }
          const row = p as { axisValue?: string; name?: string; seriesName?: string; value?: number };
          const label = row.axisValue ?? row.name ?? '';
          return `${label}<br/>${row.seriesName ?? ''}: ${row.value ?? 0}%`;
        },
      },
      grid: { left: 48, right: 24, top: 48, bottom: 40 },
      xAxis: { type: 'category', data: xs, boundaryGap: false },
      yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
      series: [
        {
          type: 'line',
          smooth: true,
          name: this.translate.instant('dashboard.charts.utilization'),
          data: ys,
          areaStyle: { opacity: 0.08 },
        },
      ],
    };
  }

  private buildTaskPie(o: DashboardOverview): EChartsCoreOption {
    const data = o.taskStatusDistribution.map((s) => ({
      name: this.translate.instant(`dashboard.taskStatus.${s.status}`),
      value: s.count,
    }));
    return {
      title: { text: this.translate.instant('dashboard.charts.taskStatus'), left: 0, textStyle: { fontSize: 14 } },
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, left: 'center' },
      series: [
        {
          type: 'pie',
          radius: ['36%', '62%'],
          center: ['50%', '46%'],
          data,
          emphasis: { itemStyle: { shadowBlur: 8, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' } },
        },
      ],
    };
  }

  private buildProjectHoursBar(
    rows: { label: string; hours: number }[],
    title: string,
  ): EChartsCoreOption {
    return {
      title: { text: title, left: 0, textStyle: { fontSize: 14 } },
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
      grid: { left: 56, right: 16, top: 48, bottom: 72 },
      xAxis: { type: 'category', data: rows.map((r) => r.label), axisLabel: { rotate: 30 } },
      yAxis: { type: 'value', name: 'h' },
      series: [{ type: 'bar', data: rows.map((r) => Number(r.hours ?? 0)) }],
    };
  }

  private buildPercentBar(rows: { label: string; percent: number }[], title: string): EChartsCoreOption {
    return {
      title: { text: title, left: 0, textStyle: { fontSize: 14 } },
      tooltip: {
        trigger: 'axis',
        formatter: (params: unknown) => {
          const p = Array.isArray(params) ? params[0] : params;
          if (!p || typeof p !== 'object') {
            return '';
          }
          const row = p as { axisValue?: string; name?: string; value?: number };
          const label = row.axisValue ?? row.name ?? '';
          return `${label}<br/>${row.value ?? 0}%`;
        },
      },
      grid: { left: 48, right: 16, top: 48, bottom: 72 },
      xAxis: { type: 'category', data: rows.map((r) => r.label), axisLabel: { rotate: 30 } },
      yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
      series: [{ type: 'bar', data: rows.map((r) => Number(r.percent ?? 0)) }],
    };
  }

  private buildWorkloadBar(rows: { label: string; percent: number }[]): EChartsCoreOption {
    const data = rows.map((r) => {
      const v = Number(r.percent ?? 0);
      return {
        value: v,
        itemStyle: v > 100 ? { color: '#cf1322' } : undefined,
      };
    });
    return {
      title: { text: this.translate.instant('dashboard.charts.workload'), left: 0, textStyle: { fontSize: 14 } },
      tooltip: {
        trigger: 'axis',
        formatter: (params: unknown) => {
          const p = Array.isArray(params) ? params[0] : params;
          if (!p || typeof p !== 'object') {
            return '';
          }
          const row = p as { axisValue?: string; name?: string; value?: unknown };
          const label = row.axisValue ?? row.name ?? '';
          const num = Dashboard.axisSeriesNumericValue(row.value);
          return `${label}<br/>${num}%`;
        },
      },
      grid: { left: 48, right: 16, top: 48, bottom: 72 },
      xAxis: { type: 'category', data: rows.map((r) => r.label), axisLabel: { rotate: 30 } },
      yAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
      series: [{ type: 'bar', data }],
    };
  }

  private static axisSeriesNumericValue(value: unknown): number {
    if (typeof value === 'number') {
      return value;
    }
    if (value && typeof value === 'object' && 'value' in value) {
      return Number((value as { value: unknown }).value) || 0;
    }
    return 0;
  }
}
