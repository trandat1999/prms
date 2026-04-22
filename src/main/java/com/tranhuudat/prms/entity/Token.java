package com.tranhuudat.prms.entity;

import com.tranhuudat.prms.enums.TokenTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_token")
public class Token extends BaseEntity {
    @Column(name = "token", unique = true)
    private String token;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TokenTypeEnum type = TokenTypeEnum.BEARER;

    @Column(name = "revoked")
    private boolean revoked;

    @Column(name = "expired")
    private boolean expired;

    @Column(name = "username")
    private String username;
}