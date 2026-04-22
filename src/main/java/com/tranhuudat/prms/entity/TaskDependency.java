package com.tranhuudat.prms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.util.UUID;

@Entity
@Table(name = "tbl_task_dependency")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskDependency extends BaseEntity {

    @Column(name = "predecessor_task_id", nullable = false)
    UUID predecessorTaskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predecessor_task_id", insertable = false, updatable = false)
    Task predecessorTask;

    @Column(name = "successor_task_id", nullable = false)
    UUID successorTaskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "successor_task_id", insertable = false, updatable = false)
    Task successorTask;
}
