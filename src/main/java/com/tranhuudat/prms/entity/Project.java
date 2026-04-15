package com.tranhuudat.prms.entity;

import com.tranhuudat.prms.enums.PriorityEnum;
import com.tranhuudat.prms.enums.ProjectStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

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
