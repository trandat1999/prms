package com.tranhuudat.prms.dto.task;

import com.tranhuudat.prms.enums.TaskStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskKanbanColumnUpdateRequest {
    @NotNull
    TaskStatusEnum status;

    @NotNull
    List<UUID> taskIds;
}

