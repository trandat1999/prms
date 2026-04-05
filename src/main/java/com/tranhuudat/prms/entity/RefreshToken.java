package com.tranhuudat.prms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="tbl_refresh_token")
public class RefreshToken extends BaseEntity{

    @Column(name = "token")
    private String token;

    @Column(name = "username")
    private String username;

    @Column(name="revoked")
    private Boolean revoked;

    @Column(name="expired")
    private Date expiration;
}
