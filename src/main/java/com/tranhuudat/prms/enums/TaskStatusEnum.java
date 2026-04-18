package com.tranhuudat.prms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatusEnum {
    TODO("TODO", "Todo"),
    IN_PROGRESS("IN_PROGRESS", "In Progress"),
    REVIEW("REVIEW", "Review"),
    TESTING("TESTING", "Testing"),
    DONE("DONE", "Done");

    private final String code;
    private final String name;
}
