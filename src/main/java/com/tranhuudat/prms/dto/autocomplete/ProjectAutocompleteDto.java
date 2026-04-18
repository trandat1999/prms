package com.tranhuudat.prms.dto.autocomplete;

import com.tranhuudat.prms.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectAutocompleteDto {
    private UUID id;
    private String code;
    private String name;

    public ProjectAutocompleteDto(Project entity) {
        if (Objects.nonNull(entity)) {
            BeanUtils.copyProperties(entity, this);
        }
    }
}

