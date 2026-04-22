package com.tranhuudat.prms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationTypeEnum {
    TASK_PREDECESSOR_DONE("TASK_PREDECESSOR_DONE"),
    TASK_ASSIGNED("TASK_ASSIGNED");

    private final String code;
}

