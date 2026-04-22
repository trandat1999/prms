package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.notification.UserNotificationSearchRequest;

import java.util.UUID;

public interface UserNotificationService {

    BaseResponse getPage(UserNotificationSearchRequest request);

    BaseResponse unreadCount();

    BaseResponse markRead(UUID id);
}
