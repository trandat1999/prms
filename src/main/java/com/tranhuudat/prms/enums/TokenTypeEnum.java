package com.tranhuudat.prms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TokenTypeEnum {
    BEARER("BEARER","Token trả về lúc đăng nhập"),
    ACTIVE_ACCOUNT("ACTIVE_ACCOUNT","Token trả về lúc đăng ký"),
    FORGOT_PASSWORD("FORGOT_PASSWORD","Token trả về lúc quên mật khẩu");
    private final String value;
    private final String description;
}
