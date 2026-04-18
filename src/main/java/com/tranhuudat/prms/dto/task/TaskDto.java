package com.tranhuudat.prms.dto.task;

import com.tranhuudat.prms.entity.Task;
import com.tranhuudat.prms.enums.PriorityEnum;
import com.tranhuudat.prms.enums.TaskStatusEnum;
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
public class TaskDto {
    UUID id;
    String code;
    String name;
    String shortDescription;
    String description;

    UUID projectId;
    String projectName;

    TaskStatusEnum status;
    Integer kanbanOrder;
    PriorityEnum priority;
    BigDecimal estimatedHours;
    BigDecimal actualHours;

    UUID assignedId;
    String assignedDisplay; // username - fullname

    String label;
    String type;

    public TaskDto(Task entity) {
        if (Objects.nonNull(entity)) {
            BeanUtils.copyProperties(entity, this);
            if (entity.getProject() != null) {
                this.projectName = entity.getProject().getName();
            }
            if (entity.getAssigned() != null) {
                String u = entity.getAssigned().getUsername();
                String f = entity.getAssigned().getFullName();
                this.assignedDisplay = (u != null ? u : "") + " - " + (f != null ? f : "");
            }
        }
    }
}

