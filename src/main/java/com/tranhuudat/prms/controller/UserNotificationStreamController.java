package com.tranhuudat.prms.controller;

import com.tranhuudat.prms.repository.NotificationRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.service.UserNotificationStreamService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserNotificationStreamController {
    UserNotificationStreamService streamService;
    UserRepository userRepository;
    NotificationRepository notificationRepository;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        UUID userId = resolveCurrentUserId();
        if (Objects.isNull(userId)) {
            // SSE không theo BaseResponse; trả emitter rồi complete ngay để client tự fallback.
            SseEmitter emitter = new SseEmitter(1L);
            emitter.complete();
            return emitter;
        }
        SseEmitter emitter = streamService.subscribe(userId);

        // gửi unread count hiện tại ngay sau khi subscribe
        long unreadCount = notificationRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("userId"), userId),
                cb.or(cb.isFalse(root.get("voided")), cb.isNull(root.get("voided"))),
                cb.isNull(root.get("readAt"))
        ));
        streamService.publishUnreadCount(userId, unreadCount);
        return emitter;
    }

    private UUID resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName())
                .map(com.tranhuudat.prms.entity.User::getId)
                .orElse(null);
    }
}

