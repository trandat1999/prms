package com.tranhuudat.prms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDto {
    private DashboardSummaryDto summary;
    @Builder.Default
    private List<DashboardMonthUtilizationDto> resourceUtilizationByMonth = new ArrayList<>();
    @Builder.Default
    private List<DashboardTaskStatusCountDto> taskStatusDistribution = new ArrayList<>();
    /** Rỗng khi không đủ dữ liệu (ít hơn 2 PYC có nhân sự đang gán task chưa Done) */
    @Builder.Default
    private List<DashboardLabeledPercentDto> topPycByResource = new ArrayList<>();
    @Builder.Default
    private List<DashboardLabeledHoursDto> otHoursByProject = new ArrayList<>();
    @Builder.Default
    private List<DashboardLabeledPercentDto> employeeWorkload = new ArrayList<>();
    @Builder.Default
    private List<DashboardPycProgressDto> pycProgress = new ArrayList<>();
}
