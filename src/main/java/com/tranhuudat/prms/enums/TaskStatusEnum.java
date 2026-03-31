package com.tranhuudat.prms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatusEnum {
    TODO("TODO", "Việc cần làm"),
    IN_PROGRESS("IN_PROGRESS", "Đang thực hiện"),
    REVIEW("REVIEW", "Đang xem xét"),
    COMPLETED("COMPLETED", "Đã hoàn thành"),
    CANCELLED("CANCELLED", "Đã hủy");

    private final String code;
    private final String name;
}
