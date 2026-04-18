package com.tranhuudat.prms.dto.user;

import com.tranhuudat.prms.util.ConstUtil;
import com.tranhuudat.prms.util.SystemMessage;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPasswordUpdateRequest {
    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    @Size(min = ConstUtil.MIN_LENGTH_PASSWORD_REQUIRED, message = SystemMessage.VALIDATION_MIN_LENGTH)
    String newPassword;
}

