package com.tranhuudat.prms.dto.task;

import com.tranhuudat.prms.entity.TaskChecklist;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskChecklistDto {
    UUID id;
    String title;
    Boolean checked;
    Integer sortOrder;
    BigDecimal estimatedHours;

    public TaskChecklistDto(TaskChecklist entity) {
        if (Objects.nonNull(entity)) {
            BeanUtils.copyProperties(entity, this);
        }
    }
}
