package com.tranhuudat.prms.dto.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author DatNuclear 16/01/2024
 * @project store-movie
 */
@Data
public class AuthRequest {
   @NotNull(message = "{app.validation.NotNull}")
   @NotBlank(message = "{app.validation.NotBlank}")
   private String username;
   @NotNull(message = "{app.validation.NotNull}")
   @NotBlank(message = "{app.validation.NotBlank}")
   private String password;
}
