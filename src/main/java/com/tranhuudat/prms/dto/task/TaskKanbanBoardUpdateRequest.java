package com.tranhuudat.prms.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskKanbanBoardUpdateRequest {
    @NotNull
    UUID projectId;

    @NotNull
    List<TaskKanbanColumnUpdateRequest> columns;
}

