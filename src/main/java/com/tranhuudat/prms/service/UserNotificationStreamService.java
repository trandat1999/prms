package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.notification.UserNotificationDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface UserNotificationStreamService {
    SseEmitter subscribe(UUID userId);

    void publishUnreadCount(UUID userId, long unreadCount);

    void publishNotification(UUID userId, UserNotificationDto item, long unreadCount);
}

