package com.tranhuudat.prms.dto.response;

import com.tranhuudat.prms.enums.PriorityEnum;
import com.tranhuudat.prms.enums.TaskStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskResponse {
    UUID id;
    String name;
    String description;
    String shortDescription;
    String code;
    UUID projectId;
    String projectName;
    TaskStatusEnum status;
    PriorityEnum priority;
    Double estimatedHours;
    Double actualHours;
    LocalDate dueDate;
    Set<UUID> assignedUserIds;
    Set<String> assignedUserNames;
}
