package com.tranhuudat.prms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {
    SUPPER_ADMIN("SUPPER_ADMIN", "Quản trị viên tối cao"),
    ADMIN("ADMIN", "Quản trị viên"),
    USER("USER", "Người dùng");

    private final String code;
    private final String name;
}
