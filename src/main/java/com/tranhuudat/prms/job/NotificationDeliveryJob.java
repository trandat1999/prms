package com.tranhuudat.prms.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranhuudat.prms.entity.Notification;
import com.tranhuudat.prms.entity.NotificationDelivery;
import com.tranhuudat.prms.enums.NotificationChannelEnum;
import com.tranhuudat.prms.enums.NotificationDeliveryStatusEnum;
import com.tranhuudat.prms.repository.NotificationDeliveryRepository;
import com.tranhuudat.prms.repository.NotificationRepository;
import com.tranhuudat.prms.service.MailService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDeliveryJob {

    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final NotificationRepository notificationRepository;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;
    private final MailService mailService;

    @Value("${app.notification.job.enabled:true}")
    private boolean jobEnabled;

    @Value("${app.notification.job.batch-size:25}")
    private int batchSize;

    @Value("${app.notification.job.max-attempts:8}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${app.notification.job.fixed-delay-ms:30000}")
    public void tick() {
        if (!jobEnabled) {
            return;
        }
        try {
            runOnce();
        } catch (Exception ex) {
            log.warn("NotificationDeliveryJob failed", ex);
        }
    }

    @Transactional
    protected void runOnce() {
        Date now = new Date();
        List<UUID> ids = notificationDeliveryRepository.claimBatch(entityManager, Math.max(1, batchSize), now);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        List<NotificationDelivery> deliveries = notificationDeliveryRepository.findByIdIn(ids).stream()
                .filter(d -> d.getVoided() == null || !d.getVoided())
                .toList();

        // Set SENDING to prevent re-claim if same tx overlaps (best-effort).
        for (NotificationDelivery d : deliveries) {
            d.setStatus(NotificationDeliveryStatusEnum.SENDING);
        }
        notificationDeliveryRepository.saveAll(deliveries);

        Map<UUID, Notification> notifications = notificationRepository.findAllById(
                        deliveries.stream().map(NotificationDelivery::getNotificationId).filter(Objects::nonNull).distinct().toList())
                .stream()
                .filter(n -> n.getVoided() == null || !n.getVoided())
                .collect(java.util.stream.Collectors.toMap(Notification::getId, n -> n, (a, b) -> a));

        for (NotificationDelivery d : deliveries) {
            try {
                Notification n = notifications.get(d.getNotificationId());
                if (n == null) {
                    fail(d, "Notification not found");
                    continue;
                }
                if (d.getChannel() == NotificationChannelEnum.IN_APP) {
                    // IN_APP should already be SENT at create-time; but keep idempotent.
                    succeed(d);
                    continue;
                }
                if (d.getChannel() == NotificationChannelEnum.EMAIL) {
                    sendEmail(d, n);
                    continue;
                }
                fail(d, "Unsupported channel: " + d.getChannel());
            } catch (Exception ex) {
                fail(d, ex.getMessage());
            }
        }
    }

    private void sendEmail(NotificationDelivery d, Notification n) throws Exception {
        String to = d.getToAddress();
        if (!StringUtils.hasText(to)) {
            fail(d, "Missing toAddress");
            return;
        }
        String subjectKey = d.getSubjectKey();
        if (!StringUtils.hasText(subjectKey)) {
            fail(d, "Missing subjectKey");
            return;
        }
        String template = d.getTemplateName();
        if (!StringUtils.hasText(template)) {
            fail(d, "Missing templateName");
            return;
        }

        Map<String, Object> model = new HashMap<>();
        if (StringUtils.hasText(d.getModelJson())) {
            model.putAll(objectMapper.readValue(d.getModelJson(), new TypeReference<Map<String, Object>>() {}));
        }

        // subject: i18n, allow args if provided
        String subject = resolveSubject(subjectKey, n.getMessageArgsJson(), model);
        model.put("subject", subject);

        Context ctx = new Context(LocaleContextHolder.getLocale());
        ctx.setVariables(model);
        String html = templateEngine.process(template, ctx);

        mailService.sendHtml(to, subject, html);
        succeed(d);
    }

    private String resolveSubject(String subjectKey, String messageArgsJson, Map<String, Object> model) {
        try {
            if (StringUtils.hasText(messageArgsJson)) {
                List<String> args = objectMapper.readValue(messageArgsJson, new TypeReference<List<String>>() {});
                // subject uses {0},{1}...; for this template we pass predCode/succCode
                return messageSource.getMessage(subjectKey, args.toArray(), LocaleContextHolder.getLocale());
            }
        } catch (Exception ignored) {
        }
        // fallback to model vars if args missing
        Object pred = model.get("predCode");
        Object succ = model.get("succCode");
        Object link = model.get("link");
        return messageSource.getMessage(subjectKey, new Object[] { pred, succ, link }, LocaleContextHolder.getLocale());
    }

    private void succeed(NotificationDelivery d) {
        d.setStatus(NotificationDeliveryStatusEnum.SENT);
        d.setSentAt(new Date());
        d.setLastError(null);
        notificationDeliveryRepository.save(d);
    }

    private void fail(NotificationDelivery d, String error) {
        int nextAttempt = (d.getAttemptCount() != null ? d.getAttemptCount() : 0) + 1;
        d.setAttemptCount(nextAttempt);
        d.setLastError(error);
        if (nextAttempt >= Math.max(1, maxAttempts)) {
            d.setStatus(NotificationDeliveryStatusEnum.FAILED);
            d.setNextAttemptAt(null);
        } else {
            d.setStatus(NotificationDeliveryStatusEnum.RETRY);
            d.setNextAttemptAt(new Date(System.currentTimeMillis() + backoff(nextAttempt).toMillis()));
        }
        notificationDeliveryRepository.save(d);
    }

    private Duration backoff(int attempt) {
        // 1m, 5m, 15m, 1h, 2h, 4h...
        return switch (attempt) {
            case 1 -> Duration.ofMinutes(1);
            case 2 -> Duration.ofMinutes(5);
            case 3 -> Duration.ofMinutes(15);
            case 4 -> Duration.ofHours(1);
            default -> Duration.ofHours(Math.min(12, 1L << Math.min(5, attempt - 4)));
        };
    }
}

