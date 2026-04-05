package com.tranhuudat.prms.dto.authentication;


import com.tranhuudat.prms.util.ConstUtil;
import com.tranhuudat.prms.util.SystemMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @author DatNuclear 21/01/2024
 * @project store-movie
 */
@Data
public class ForgotPasswordRequest {
    @Pattern(regexp = ConstUtil.PATTERN_USERNAME, message = SystemMessage.VALIDATION_USERNAME_PATTERN)
    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    @NotBlank(message = SystemMessage.VALIDATION_NOT_BLANK)
    private String username;
    @Email(message = SystemMessage.VALIDATION_EMAIL)
    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    @NotBlank(message = SystemMessage.VALIDATION_NOT_BLANK)
    private String email;
}
