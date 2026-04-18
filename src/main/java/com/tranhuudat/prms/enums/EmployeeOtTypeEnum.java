package com.tranhuudat.prms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public enum EmployeeOtTypeEnum {
    WEEKDAY(new BigDecimal("1.5")),
    WEEKEND(new BigDecimal("2.0")),
    HOLIDAY(new BigDecimal("3.0"));

    private final BigDecimal coefficient;
}
