package com.tranhuudat.prms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    private long totalPyc;
    private long activePyc;
    private long totalTask;
    private long taskDone;
    /** OT đã duyệt trong tháng hiện tại (giờ) */
    private BigDecimal otHoursMonth;
    /** % sử dụng nguồn lực trung bình tháng hiện tại (theo phân bổ) */
    private BigDecimal resourceUsagePercent;
}
