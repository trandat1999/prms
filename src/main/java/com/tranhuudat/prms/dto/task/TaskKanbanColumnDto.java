package com.tranhuudat.prms.dto.task;

import com.tranhuudat.prms.enums.TaskStatusEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskKanbanColumnDto {
    TaskStatusEnum status;
    String name;
    List<TaskDto> tasks;
}

