package com.tranhuudat.prms.dto.task;

import com.tranhuudat.prms.enums.TaskStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskStatusUpdateRequest {
    @NotNull
    TaskStatusEnum status;
}

