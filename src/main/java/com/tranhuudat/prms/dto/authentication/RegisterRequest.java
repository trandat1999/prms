package com.tranhuudat.prms.dto.authentication;

import com.tranhuudat.prms.util.ConstUtil;
import com.tranhuudat.prms.util.SystemMessage;
import com.tranhuudat.prms.util.SystemVariable;
import com.tranhuudat.prms.util.anotation.FieldMatch;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * @author DatNuclear 19/01/2024
 * @project store-movie
 */
@Data
@FieldMatch(message = SystemMessage.VALIDATION_FIELD_MATCH,
        fieldName = SystemVariable.CONFIRM_PASSWORD,
        dependFieldName = SystemVariable.PASSWORD)
public class RegisterRequest {
    @Pattern(regexp = ConstUtil.PATTERN_USERNAME, message = SystemMessage.VALIDATION_USERNAME_PATTERN)
    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    @NotBlank(message = SystemMessage.VALIDATION_NOT_BLANK)
    private String username;
    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    @Size(min = ConstUtil.MIN_LENGTH_PASSWORD_REQUIRED, message = SystemMessage.VALIDATION_MIN_LENGTH)
    private String password;
    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    @Size(min = ConstUtil.MIN_LENGTH_PASSWORD_REQUIRED, message = SystemMessage.VALIDATION_MIN_LENGTH)
    private String confirmPassword;
    @Email(message = SystemMessage.VALIDATION_EMAIL)
    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    @NotBlank(message = SystemMessage.VALIDATION_NOT_BLANK)
    private String email;
    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    @NotBlank(message = SystemMessage.VALIDATION_NOT_BLANK)
    private String fullName;
}
