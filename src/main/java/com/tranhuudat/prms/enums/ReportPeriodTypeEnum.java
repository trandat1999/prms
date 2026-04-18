package com.tranhuudat.prms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportPeriodTypeEnum {
    MONTH("MONTH"),
    QUARTER("QUARTER"),
    YEAR("YEAR");

    private final String code;
}
