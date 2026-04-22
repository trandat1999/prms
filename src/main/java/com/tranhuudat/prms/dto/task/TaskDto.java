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
import java.util.Date;
import java.util.List;
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
    /** Manager của project chứa task (phục vụ check quyền UI). */
    UUID projectManagerId;

    TaskStatusEnum status;
    Integer kanbanOrder;
    PriorityEnum priority;
    BigDecimal estimatedHours;
    BigDecimal actualHours;

    UUID assignedId;
    String assignedDisplay; // username - fullname

    UUID reporterId;
    String reporterDisplay;

    UUID reviewerId;
    String reviewerDisplay;

    UUID parentTaskId;
    String parentTaskCode;

    Date dueDate;
    Date startedAt;
    Date completedAt;
    String blockedReason;
    String taskCategory;
    Integer storyPoint;

    String label;
    String type;

    List<TaskChecklistDto> checklists;
    List<UUID> predecessorTaskIds;
    List<TaskRefDto> predecessors;

    // Kanban summary (để hiển thị nhanh, không cần load checklist full)
    Long checklistTotalCount;
    Long checklistDoneCount;

    public TaskDto(Task entity) {
        if (Objects.nonNull(entity)) {
            BeanUtils.copyProperties(entity, this);
            if (entity.getProject() != null) {
                this.projectName = entity.getProject().getName();
                this.projectManagerId = entity.getProject().getManagerId();
            }
            if (entity.getAssigned() != null) {
                String u = entity.getAssigned().getUsername();
                String f = entity.getAssigned().getFullName();
                this.assignedDisplay = (u != null ? u : "") + " - " + (f != null ? f : "");
            }
            if (entity.getReporter() != null) {
                String u = entity.getReporter().getUsername();
                String f = entity.getReporter().getFullName();
                this.reporterDisplay = (u != null ? u : "") + " - " + (f != null ? f : "");
            }
            if (entity.getReviewer() != null) {
                String u = entity.getReviewer().getUsername();
                String f = entity.getReviewer().getFullName();
                this.reviewerDisplay = (u != null ? u : "") + " - " + (f != null ? f : "");
            }
            if (entity.getParentTask() != null) {
                this.parentTaskCode = entity.getParentTask().getCode();
            }
        }
    }
}

