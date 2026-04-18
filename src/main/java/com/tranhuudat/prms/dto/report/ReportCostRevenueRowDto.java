package com.tranhuudat.prms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCostRevenueRowDto {
    /** Nhãn hiển thị: yyyy-MM | yyyy-Qn | yyyy */
    private String periodLabel;
    private BigDecimal resourceCost;
    private BigDecimal pycRevenue;
    private BigDecimal profit;
}
