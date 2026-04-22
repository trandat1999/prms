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

import java.util.UUID;

@Entity
@Table(name = "tbl_push_subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PushSubscription extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    UUID userId;

    @Column(name = "endpoint", columnDefinition = "TEXT", nullable = false)
    String endpoint;

    @Column(name = "p256dh", columnDefinition = "TEXT", nullable = false)
    String p256dh;

    @Column(name = "auth", columnDefinition = "TEXT", nullable = false)
    String auth;

    @Column(name = "expiration_time")
    Long expirationTime;
}

