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
@Table(name = "tbl_app_param")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppParam extends BaseEntity {
    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "param_group", length = 100)
    String paramGroup;

    @Column(name = "param_name")
    String paramName;

    @Column(name = "param_value")
    String paramValue;

    @Column(name = "param_type", length = 50)
    String paramType;
}

