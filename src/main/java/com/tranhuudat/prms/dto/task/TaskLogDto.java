package com.tranhuudat.prms.dto.task;

import com.tranhuudat.prms.entity.TaskLog;
import com.tranhuudat.prms.enums.TaskLogActionEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskLogDto {
    UUID id;
    UUID taskId;
    TaskLogActionEnum action;
    String oldValue;
    String newValue;
    Date createdDate;
    String createdBy;

    public TaskLogDto(TaskLog entity) {
        if (Objects.nonNull(entity)) {
            BeanUtils.copyProperties(entity, this);
        }
    }
}

