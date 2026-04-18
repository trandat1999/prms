package com.tranhuudat.prms.dto.app_param;

import com.tranhuudat.prms.entity.AppParam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;

import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppParamDto {
    UUID id;

    String description;

    @NotNull
    @NotBlank
    String paramGroup;

    @NotNull
    @NotBlank
    String paramName;

    String paramValue;

    String paramType;

    public AppParamDto(AppParam entity) {
        if (Objects.nonNull(entity)) {
            BeanUtils.copyProperties(entity, this);
        }
    }
}

