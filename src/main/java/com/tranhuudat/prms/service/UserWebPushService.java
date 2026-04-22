package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.notification.UserNotificationDto;

import java.util.UUID;

public interface UserWebPushService {
    void push(UUID userId, UserNotificationDto dto);
}

