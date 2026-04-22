package com.tranhuudat.prms.entity;

import com.tranhuudat.prms.enums.PriorityEnum;
import com.tranhuudat.prms.enums.TaskStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "tbl_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Task extends BaseInformation {

    @Column(name = "project_id")
    UUID projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    TaskStatusEnum status;

    @Column(name = "kanban_order")
    Integer kanbanOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    PriorityEnum priority;

    @Column(name = "estimated_hours")
    BigDecimal estimatedHours;

    @Column(name = "actual_hours")
    BigDecimal actualHours;

    @Column(name = "assigned_id")
    UUID assignedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_id", insertable = false, updatable = false)
    User assigned;

    @Column(name = "reporter_id")
    UUID reporterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", insertable = false, updatable = false)
    User reporter;

    @Column(name = "reviewer_id")
    UUID reviewerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", insertable = false, updatable = false)
    User reviewer;

    @Column(name = "parent_task_id")
    UUID parentTaskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id", insertable = false, updatable = false)
    Task parentTask;

    @Column(name = "due_date")
    Date dueDate;

    @Column(name = "started_at")
    Date startedAt;

    @Column(name = "completed_at")
    Date completedAt;

    @Column(name = "blocked_reason")
    String blockedReason;

    @Column(name = "task_category", length = 50)
    String taskCategory;

    @Column(name = "story_point")
    Integer storyPoint;

    @Column(name = "label", length = 50)
    String label;

    @Column(name = "type", length = 50)
    String type;
}

