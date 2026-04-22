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

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tbl_task_checklist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskChecklist extends BaseEntity {

    @Column(name = "task_id", nullable = false)
    UUID taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    Task task;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "checked")
    Boolean checked;

    @Column(name = "sort_order")
    Integer sortOrder;

    @Column(name = "estimated_hours")
    BigDecimal estimatedHours;
}
