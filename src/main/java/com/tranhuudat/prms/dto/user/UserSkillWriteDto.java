package com.tranhuudat.prms.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSkillWriteDto {
    @NotNull
    UUID skillId;

    @NotNull
    @NotBlank
    String level;

    BigDecimal experienceYear;

    LocalDate lastUsedDate;

    Boolean isPrimary;
}

