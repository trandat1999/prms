package com.tranhuudat.prms.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.notification.UserNotificationDto;
import com.tranhuudat.prms.dto.notification.UserNotificationSearchRequest;
import com.tranhuudat.prms.entity.Notification;
import com.tranhuudat.prms.entity.Task;
import com.tranhuudat.prms.repository.TaskRepository;
import com.tranhuudat.prms.repository.NotificationRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.UserNotificationService;
import com.tranhuudat.prms.util.SystemMessage;
import com.tranhuudat.prms.util.SystemVariable;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl extends BaseService implements UserNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getPage(UserNotificationSearchRequest request) {
        UUID userId = resolveCurrentUserId();
        if (Objects.isNull(userId)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST));
        }
        Specification<Notification> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            predicates.add(cb.or(cb.isFalse(root.get("voided")), cb.isNull(root.get("voided"))));
            if (Objects.nonNull(request.getRead())) {
                if (Boolean.TRUE.equals(request.getRead())) {
                    predicates.add(cb.isNotNull(root.get("readAt")));
                } else {
                    predicates.add(cb.isNull(root.get("readAt")));
                }
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Page<Notification> page = notificationRepository.findAll(spec, getPageable(request));
        Map<UUID, String> taskCodes = new HashMap<>();
        List<UUID> taskIds = page.getContent().stream()
                .map(Notification::getRelatedTaskId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!taskIds.isEmpty()) {
            taskCodes.putAll(taskRepository.findAllById(taskIds).stream()
                    .filter(t -> t.getVoided() == null || !t.getVoided())
                    .collect(Collectors.toMap(Task::getId, Task::getCode, (a, b) -> a)));
        }
        Page<UserNotificationDto> mapped = page.map(n -> new UserNotificationDto(
                n,
                resolveMessage(n),
                n.getRelatedTaskId() != null ? taskCodes.get(n.getRelatedTaskId()) : null));
        return getResponse200(mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse unreadCount() {
        UUID userId = resolveCurrentUserId();
        if (Objects.isNull(userId)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST));
        }
        Specification<Notification> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("userId"), userId),
                cb.or(cb.isFalse(root.get("voided")), cb.isNull(root.get("voided"))),
                cb.isNull(root.get("readAt"))
        );
        return getResponse200(notificationRepository.count(spec));
    }

    @Override
    @Transactional
    public BaseResponse markRead(UUID id) {
        UUID userId = resolveCurrentUserId();
        if (Objects.isNull(userId)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST));
        }
        Notification entity = notificationRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.NOTIFICATION)));
        }
        if (!Objects.equals(entity.getUserId(), userId)) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.NOTIFICATION)));
        }
        entity.setReadAt(new Date());
        notificationRepository.save(entity);
        String taskCode = entity.getRelatedTaskId() != null
                ? taskRepository.findById(entity.getRelatedTaskId()).map(Task::getCode).orElse(null)
                : null;
        return getResponse200(new UserNotificationDto(entity, resolveMessage(entity), taskCode));
    }

    private String resolveMessage(Notification n) {
        if (!StringUtils.hasText(n.getMessageKey())) {
            return "";
        }
        if (!StringUtils.hasText(n.getMessageArgsJson())) {
            return getMessage(n.getMessageKey());
        }
        try {
            List<String> args = objectMapper.readValue(n.getMessageArgsJson(), new TypeReference<List<String>>() {});
            return getMessage(n.getMessageKey(), args.toArray());
        } catch (Exception ex) {
            return getMessage(n.getMessageKey());
        }
    }

    private UUID resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).map(com.tranhuudat.prms.entity.User::getId).orElse(null);
    }
}
