package com.tranhuudat.prms.dto.user;

import com.tranhuudat.prms.util.SystemMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CurrentUserProfileUpdateRequest {
    @Email(message = SystemMessage.VALIDATION_EMAIL)
    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    @NotBlank(message = SystemMessage.VALIDATION_NOT_BLANK)
    String email;

    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    @NotBlank(message = SystemMessage.VALIDATION_NOT_BLANK)
    String fullName;
}
