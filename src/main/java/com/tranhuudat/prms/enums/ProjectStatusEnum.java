package com.tranhuudat.prms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectStatusEnum {
    NOT_STARTED("NOT_STARTED", "Chưa bắt đầu"),
    IN_PROGRESS("IN_PROGRESS", "Đang thực hiện"),
    COMPLETED("COMPLETED", "Đã hoàn thành"),
    ON_HOLD("ON_HOLD", "Tạm dừng"),
    CANCELLED("CANCELLED", "Đã hủy");

    private final String code;
    private final String name;
}
