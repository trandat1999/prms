package com.tranhuudat.prms.entity;

import com.tranhuudat.prms.enums.NotificationTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "tbl_notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 80)
    NotificationTypeEnum type;

    @Column(name = "message_key", nullable = false, length = 200)
    String messageKey;

    @Column(name = "message_args_json", columnDefinition = "TEXT")
    String messageArgsJson;

    @Column(name = "related_project_id")
    UUID relatedProjectId;

    @Column(name = "related_task_id")
    UUID relatedTaskId;

    @Column(name = "read_at")
    Date readAt;
}

