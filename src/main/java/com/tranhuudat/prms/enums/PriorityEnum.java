package com.tranhuudat.prms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PriorityEnum {
    LOW("LOW", "Thấp"),
    MEDIUM("MEDIUM", "Trung bình"),
    HIGH("HIGH", "Cao"),
    URGENT("URGENT", "Khẩn cấp");

    private final String code;
    private final String name;
}
