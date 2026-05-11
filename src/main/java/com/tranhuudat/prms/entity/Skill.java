package com.tranhuudat.prms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tbl_skill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Skill extends BaseEntity {
    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "category", length = 100)
    String category;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "code", length = 100, nullable = false, unique = true)
    String code;
}

