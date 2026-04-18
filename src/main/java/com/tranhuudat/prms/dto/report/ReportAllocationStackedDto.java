package com.tranhuudat.prms.dto.report;

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
public class ReportAllocationStackedDto {
    @Builder.Default
    private List<String> periodLabels = new ArrayList<>();
    @Builder.Default
    private List<ReportStackSeriesDto> series = new ArrayList<>();
}
