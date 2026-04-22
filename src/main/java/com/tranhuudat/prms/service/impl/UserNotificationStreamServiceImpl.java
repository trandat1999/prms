package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.notification.UserNotificationDto;
import com.tranhuudat.prms.dto.notification.UserNotificationStreamEvent;
import com.tranhuudat.prms.service.UserNotificationStreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class UserNotificationStreamServiceImpl implements UserNotificationStreamService {
    /**
     * Giữ emitter theo userId. 1 user có thể mở nhiều tab.
     */
    private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    private static final long TIMEOUT_MS = Duration.ofMinutes(30).toMillis();

    @Override
    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable cleanup = () -> removeEmitter(userId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError((ex) -> cleanup.run());

        // "hello" để client biết kết nối thành công
        try {
            emitter.send(SseEmitter.event()
                    .name("ready")
                    .data(UserNotificationStreamEvent.builder().type("ready").build()));
        } catch (IOException ex) {
            cleanup.run();
        }
        return emitter;
    }

    @Override
    public void publishUnreadCount(UUID userId, long unreadCount) {
        send(userId, "unreadCount", UserNotificationStreamEvent.builder()
                .type("unreadCount")
                .unreadCount(unreadCount)
                .build());
    }

    @Override
    public void publishNotification(UUID userId, UserNotificationDto item, long unreadCount) {
        send(userId, "notification", UserNotificationStreamEvent.builder()
                .type("notification")
                .unreadCount(unreadCount)
                .item(item)
                .build());
    }

    private void send(UUID userId, String eventName, UserNotificationStreamEvent payload) {
        if (Objects.isNull(userId)) {
            return;
        }
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(userId);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload));
            } catch (Exception ex) {
                removeEmitter(userId, emitter);
            }
        }
    }

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(userId);
        if (list == null) {
            return;
        }
        list.remove(emitter);
        if (list.isEmpty()) {
            emitters.remove(userId);
        }
        try {
            emitter.complete();
        } catch (Exception ignore) {
            // ignore
        }
    }
}

