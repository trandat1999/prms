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
public class DashboardMonthUtilizationDto {
    /** Định dạng yyyy-MM */
    private String month;
    private BigDecimal utilizationPercent;
}
