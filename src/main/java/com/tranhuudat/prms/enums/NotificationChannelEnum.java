package com.tranhuudat.prms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationChannelEnum {
    IN_APP("IN_APP"),
    EMAIL("EMAIL");

    private final String code;
}

