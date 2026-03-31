package com.tranhuudat.prms.dto.request;

import com.tranhuudat.prms.enums.PriorityEnum;
import com.tranhuudat.prms.enums.TaskStatusEnum;
import jakarta.validation.constraints.NotBlank;
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
public class TaskRequest {
    @NotBlank(message = "NAME_INVALID")
    String name;

    String description;
    String shortDescription;

    @NotBlank(message = "CODE_INVALID")
    String code;

    UUID projectId;
    TaskStatusEnum status;
    PriorityEnum priority;
    Double estimatedHours;
    Double actualHours;
    LocalDate dueDate;
    Set<UUID> assignedUserIds;
}
