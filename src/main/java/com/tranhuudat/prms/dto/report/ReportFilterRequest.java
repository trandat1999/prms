package com.tranhuudat.prms.dto.report;

import com.tranhuudat.prms.enums.ReportPeriodTypeEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportFilterRequest {

    @NotNull
    ReportPeriodTypeEnum periodType;

    @NotNull
    @Min(2000)
    @Max(2100)
    Integer year;
}
