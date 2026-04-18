package com.tranhuudat.prms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTaskWeekTrendDto {
    /** Ví dụ 2026-W14 */
    private String weekLabel;
    private long tasksDone;
}
