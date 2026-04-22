package com.tranhuudat.prms.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload cho SSE stream (tab đang mở) để cập nhật badge/toast realtime.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationStreamEvent {
    /**
     * Kiểu event phía client (vd: "unreadCount", "notification").
     */
    private String type;

    /**
     * Unread count hiện tại (nếu có).
     */
    private Long unreadCount;

    /**
     * Thông báo mới (nếu có).
     */
    private UserNotificationDto item;
}

