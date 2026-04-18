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
public class ReportProjectPerformanceDto {
    private String projectCode;
    private String projectName;
    private long tasksDone;
    private BigDecimal resourceSharePercent;
    private BigDecimal otHours;
}
