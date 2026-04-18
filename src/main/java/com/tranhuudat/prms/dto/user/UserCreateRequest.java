package com.tranhuudat.prms.dto.user;

import com.tranhuudat.prms.util.ConstUtil;
import com.tranhuudat.prms.util.SystemMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {
    @Pattern(regexp = ConstUtil.PATTERN_USERNAME, message = SystemMessage.VALIDATION_USERNAME_PATTERN)
    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    @NotBlank(message = SystemMessage.VALIDATION_NOT_BLANK)
    String username;

    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    @Size(min = ConstUtil.MIN_LENGTH_PASSWORD_REQUIRED, message = SystemMessage.VALIDATION_MIN_LENGTH)
    String password;

    @Email(message = SystemMessage.VALIDATION_EMAIL)
    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    @NotBlank(message = SystemMessage.VALIDATION_NOT_BLANK)
    String email;

    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    @NotBlank(message = SystemMessage.VALIDATION_NOT_BLANK)
    String fullName;

    /**
     * Set mã role (vd: SUPPER_ADMIN, USER...). Nếu null/empty sẽ set mặc định USER.
     */
    Set<String> roleCodes;

    /**
     * Null = mặc định true.
     */
    Boolean enabled;
}

