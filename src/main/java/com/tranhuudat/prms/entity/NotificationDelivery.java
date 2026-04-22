package com.tranhuudat.prms.entity;

import com.tranhuudat.prms.enums.NotificationChannelEnum;
import com.tranhuudat.prms.enums.NotificationDeliveryStatusEnum;
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

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "tbl_notification_delivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationDelivery extends BaseEntity {

    @Column(name = "notification_id", nullable = false)
    UUID notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", insertable = false, updatable = false)
    Notification notification;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 40)
    NotificationChannelEnum channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    NotificationDeliveryStatusEnum status;

    @Column(name = "attempt_count")
    Integer attemptCount;

    @Column(name = "next_attempt_at")
    Date nextAttemptAt;

    @Column(name = "sent_at")
    Date sentAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    String lastError;

    @Column(name = "to_address", length = 255)
    String toAddress;

    @Column(name = "template_name", length = 200)
    String templateName;

    @Column(name = "subject_key", length = 200)
    String subjectKey;

    @Column(name = "model_json", columnDefinition = "TEXT")
    String modelJson;
}

