package com.tranhuudat.prms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationDeliveryStatusEnum {
    PENDING("PENDING"),
    SENDING("SENDING"),
    SENT("SENT"),
    RETRY("RETRY"),
    FAILED("FAILED");

    private final String code;
}

