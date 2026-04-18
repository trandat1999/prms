package com.tranhuudat.prms.dto.report;

import com.tranhuudat.prms.enums.ReportPeriodTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDataDto {
    private ReportPeriodTypeEnum periodType;
    private Integer year;
    /** Đơn vị hiển thị giá trị PYC & nhân lực: MM */
    @Builder.Default
    private String valueUnit = "MM";
    /**
     * Hệ số MM cho 1 FTE tháng đầy đủ (sau khi tính Σ (ngày làm/T2–T6 trong giao hiệu × % phân bổ / 100)).
     * Cấu hình App param nhóm REPORT, tên REPORT_LABOR_MM_PER_FULL_FTE_MONTH (mặc định 1).
     */
    private BigDecimal laborMmPerFullFteMonth;

    @Builder.Default
    private List<ReportCostRevenueRowDto> costVsRevenue = new ArrayList<>();
    private ReportAllocationStackedDto allocationStacked;
    @Builder.Default
    private List<ReportPersonnelPerformanceDto> personnelPerformance = new ArrayList<>();
    @Builder.Default
    private List<ReportUserOtMonthDto> otByUserMonth = new ArrayList<>();
    @Builder.Default
    private List<ReportProjectPerformanceDto> projectPerformance = new ArrayList<>();
    @Builder.Default
    private List<ReportTaskWeekTrendDto> taskCompletionTrend = new ArrayList<>();
}
