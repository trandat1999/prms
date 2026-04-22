package com.tranhuudat.prms.dto.notification;

import com.tranhuudat.prms.entity.Notification;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserNotificationDto {
    UUID id;
    String message;
    boolean read;
    Date createdDate;
    UUID relatedProjectId;
    UUID relatedTaskId;
    String relatedTaskCode;

    public UserNotificationDto(Notification entity, String resolvedMessage, String relatedTaskCode) {
        if (Objects.nonNull(entity)) {
            this.id = entity.getId();
            this.createdDate = entity.getCreatedDate();
            this.relatedProjectId = entity.getRelatedProjectId();
            this.relatedTaskId = entity.getRelatedTaskId();
            this.message = resolvedMessage;
            this.read = entity.getReadAt() != null;
            this.relatedTaskCode = relatedTaskCode;
        }
    }
}
