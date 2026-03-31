package com.tranhuudat.prms.entity;

import com.tranhuudat.prms.enums.PriorityEnum;
import com.tranhuudat.prms.enums.TaskStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tbl_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Task extends BaseInformation {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    TaskStatusEnum status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    PriorityEnum priority;

    @Column(name = "estimated_hours")
    Double estimatedHours;

    @Column(name = "actual_hours")
    Double actualHours;

    @Column(name = "due_date")
    LocalDate dueDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "task_assignment",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    Set<User> assignedUsers = new HashSet<>();
}
