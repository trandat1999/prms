package com.tranhuudat.prms.entity;

import java.util.Date;
import java.math.BigDecimal;
import java.util.UUID;

import com.tranhuudat.prms.enums.PriorityEnum;
import com.tranhuudat.prms.enums.ProjectStatusEnum;

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

@Entity
@Table(name = "tbl_project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Project extends BaseInformation {

    @Column(name = "manager_id")
    UUID managerId;

    @Column(name = "project_value")
    BigDecimal projectValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id",insertable = false,updatable = false)
    User manager;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    PriorityEnum priority;

    @Column(name = "start_date")
    Date startDate;

    @Column(name = "end_date")
    Date endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    ProjectStatusEnum status;

    @Column(name = "progress_percentage")
    Double progressPercentage;
}
