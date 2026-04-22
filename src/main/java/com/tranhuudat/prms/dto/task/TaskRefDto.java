package com.tranhuudat.prms.dto.task;

import com.tranhuudat.prms.entity.Task;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskRefDto {
    UUID id;
    String code;
    String name;

    public TaskRefDto(Task entity) {
        if (Objects.nonNull(entity)) {
            this.id = entity.getId();
            this.code = entity.getCode();
            this.name = entity.getName();
        }
    }
}
