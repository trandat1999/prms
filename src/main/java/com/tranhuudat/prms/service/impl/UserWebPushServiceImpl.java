package com.tranhuudat.prms.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranhuudat.prms.config.AppProperties;
import com.tranhuudat.prms.dto.notification.UserNotificationDto;
import com.tranhuudat.prms.entity.PushSubscription;
import com.tranhuudat.prms.repository.PushSubscriptionRepository;
import com.tranhuudat.prms.service.UserWebPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.client.methods.HttpPost;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Security;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserWebPushServiceImpl implements UserWebPushService {
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    private volatile PushService pushService;

    @Override
    public void push(UUID userId, UserNotificationDto dto) {
        if (Objects.isNull(userId) || dto == null) {
            return;
        }
        PushService service = getOrCreatePushService();
        if (service == null) {
            log.debug("WebPush skipped: pushService is null");
            return;
        }
        List<PushSubscription> subs = pushSubscriptionRepository.findByUserIdAndVoidedFalse(userId);
        if (subs.isEmpty()) {
            log.debug("WebPush skipped: no subscription for user {}", userId);
            return;
        }

        String url = buildUrl(dto);
        Map<String, Object> payload = new HashMap<>();
        payload.put("notification", Map.of(
                "title", "PRMS",
                "body", dto.getMessage(),
                "data", Map.of(
                        "onActionClick", Map.of(
                                "default", Map.of(
                                        "operation", "navigateLastFocusedOrOpen",
                                        "url", url
                                )
                        )
                )
        ));

        for (PushSubscription s : subs) {
            try {
                Subscription subscription = new Subscription();
                subscription.endpoint = s.getEndpoint();
                subscription.keys = new Subscription.Keys(s.getP256dh(), s.getAuth());
                String json = objectMapper.writeValueAsString(payload);
                Notification n = new Notification(subscription, json);
                org.apache.http.HttpResponse res = service.send(n);
                int status = res != null && res.getStatusLine() != null ? res.getStatusLine().getStatusCode() : 0;
                String reason = res != null && res.getStatusLine() != null ? res.getStatusLine().getReasonPhrase() : "";
                String body = "";
                try {
                    if (res != null && res.getEntity() != null) {
                        body = EntityUtils.toString(res.getEntity());
                    }
                } catch (Exception ignore) {
                    // ignore
                }
                log.debug("WebPush response status={} reason='{}' subscription={} body={}", status, reason, s.getId(), body);
                // 404/410: subscription không còn hợp lệ -> void để không gửi lại
                if (status == 404 || status == 410) {
                    s.setVoided(true);
                    pushSubscriptionRepository.save(s);
                }
            } catch (Exception ex) {
                log.warn("WebPush failed for subscription {}", s.getId(), ex);
            }
        }
    }

    private PushService getOrCreatePushService() {
        if (pushService != null) {
            return pushService;
        }
        synchronized (this) {
            if (pushService != null) {
                return pushService;
            }
            if (!StringUtils.hasText(appProperties.getPushVapidPublicKey())
                    || !StringUtils.hasText(appProperties.getPushVapidPrivateKey())) {
                log.info("WebPush disabled: missing VAPID keys");
                return null;
            }
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            try {
                PushService service = new PushService(
                        appProperties.getPushVapidPublicKey(),
                        appProperties.getPushVapidPrivateKey(),
                        appProperties.getPushSubject()
                );
                this.pushService = service;
                return service;
            } catch (Exception ex) {
                log.warn("Could not init PushService", ex);
                return null;
            }
        }
    }

    private String buildUrl(UserNotificationDto dto) {
        if (dto.getRelatedTaskId() != null || StringUtils.hasText(dto.getRelatedTaskCode())) {
            StringBuilder sb = new StringBuilder("/kanban");
            boolean first = true;
            if (dto.getRelatedProjectId() != null) {
                sb.append(first ? "?" : "&").append("projectId=").append(dto.getRelatedProjectId());
                first = false;
            }
            if (dto.getRelatedTaskId() != null) {
                sb.append(first ? "?" : "&").append("taskId=").append(dto.getRelatedTaskId());
                first = false;
            }
            if (StringUtils.hasText(dto.getRelatedTaskCode())) {
                sb.append(first ? "?" : "&").append("taskCode=").append(dto.getRelatedTaskCode());
            }
            return sb.toString();
        }
        if (dto.getRelatedProjectId() != null) {
            return "/project/" + dto.getRelatedProjectId() + "/tasks";
        }
        return "/dashboard";
    }
}

