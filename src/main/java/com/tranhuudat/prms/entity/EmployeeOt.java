package com.tranhuudat.prms.entity;

import com.tranhuudat.prms.enums.EmployeeOtStatusEnum;
import com.tranhuudat.prms.enums.EmployeeOtTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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
@Table(name = "employee_ot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeOt extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    User user;

    @Column(name = "project_id")
    UUID projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    Project project;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ot_date", nullable = false)
    Date otDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    Date endTime;

    @Column(name = "ot_hours")
    BigDecimal otHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "ot_type", length = 50)
    EmployeeOtTypeEnum otType;

    @Column(name = "reason", columnDefinition = "TEXT")
    String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    EmployeeOtStatusEnum status;

    @Column(name = "approved_by")
    UUID approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", insertable = false, updatable = false)
    User approver;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "approved_date")
    Date approvedDate;
}
