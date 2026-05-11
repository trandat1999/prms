package com.tranhuudat.prms.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSkillsUpdateRequest {
    @NotNull
    List<UserSkillWriteDto> items;
}

