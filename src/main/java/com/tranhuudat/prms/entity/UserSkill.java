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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tbl_user_skill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSkill extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    UUID userId;

    @Column(name = "skill_id", nullable = false)
    UUID skillId;

    @Column(name = "level", length = 50, nullable = false)
    String level;

    @Column(name = "experience_year")
    BigDecimal experienceYear;

    @Column(name = "last_used_date")
    LocalDate lastUsedDate;

    @Column(name = "is_primary")
    Boolean isPrimary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", insertable = false, updatable = false)
    Skill skill;
}

