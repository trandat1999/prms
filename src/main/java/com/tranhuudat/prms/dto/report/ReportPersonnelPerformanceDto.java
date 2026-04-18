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
public class ReportPersonnelPerformanceDto {
    private String userLabel;
    private long tasksDone;
    private BigDecimal otHours;
    /** Giá trị nhân lực (MM) trong kỳ: Σ tháng (ngày làm thực tế / tổng ngày T2–T6 tháng × % phân bổ / 100) × hệ số laborMmPerFullFteMonth */
    private BigDecimal laborMm;
}
