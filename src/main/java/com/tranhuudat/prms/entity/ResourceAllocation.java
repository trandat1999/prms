package com.tranhuudat.prms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "resource_allocation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResourceAllocation extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    User user;

    @Column(name = "role", length = 50)
    String role;

    /** Tháng phân bổ (cột DB: month) */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "month")
    Date resourceMonth;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_date")
    Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date")
    Date endDate;

    @Column(name = "allocation_percent")
    BigDecimal allocationPercent;
}
