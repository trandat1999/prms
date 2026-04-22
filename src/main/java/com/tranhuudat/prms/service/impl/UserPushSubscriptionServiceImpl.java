package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.notification.PushSubscriptionDeleteRequest;
import com.tranhuudat.prms.dto.notification.PushSubscriptionUpsertRequest;
import com.tranhuudat.prms.entity.PushSubscription;
import com.tranhuudat.prms.repository.PushSubscriptionRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.UserPushSubscriptionService;
import com.tranhuudat.prms.util.SystemMessage;
import com.tranhuudat.prms.util.SystemVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPushSubscriptionServiceImpl extends BaseService implements UserPushSubscriptionService {
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BaseResponse upsert(PushSubscriptionUpsertRequest request) {
        UUID userId = resolveCurrentUserId();
        if (Objects.isNull(userId) || request == null || !StringUtils.hasText(request.getEndpoint())
                || request.getKeys() == null
                || !StringUtils.hasText(request.getKeys().getP256dh())
                || !StringUtils.hasText(request.getKeys().getAuth())) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST));
        }
        PushSubscription entity = pushSubscriptionRepository.findByEndpoint(request.getEndpoint()).orElse(null);
        if (entity == null) {
            entity = PushSubscription.builder()
                    .userId(userId)
                    .endpoint(request.getEndpoint())
                    .p256dh(request.getKeys().getP256dh())
                    .auth(request.getKeys().getAuth())
                    .expirationTime(request.getExpirationTime())
                    .voided(false)
                    .build();
        } else {
            entity.setUserId(userId);
            entity.setP256dh(request.getKeys().getP256dh());
            entity.setAuth(request.getKeys().getAuth());
            entity.setExpirationTime(request.getExpirationTime());
            entity.setVoided(false);
        }
        pushSubscriptionRepository.save(entity);
        return getResponse200(true);
    }

    @Override
    @Transactional
    public BaseResponse delete(PushSubscriptionDeleteRequest request) {
        UUID userId = resolveCurrentUserId();
        if (Objects.isNull(userId) || request == null || !StringUtils.hasText(request.getEndpoint())) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST));
        }
        PushSubscription entity = pushSubscriptionRepository.findByEndpoint(request.getEndpoint()).orElse(null);
        if (entity == null || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse200(true);
        }
        if (!Objects.equals(entity.getUserId(), userId)) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.SUBSCRIPTION)));
        }
        entity.setVoided(true);
        pushSubscriptionRepository.save(entity);
        return getResponse200(true);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse mySubscriptions() {
        UUID userId = resolveCurrentUserId();
        if (Objects.isNull(userId)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST));
        }
        List<PushSubscription> subs = pushSubscriptionRepository.findByUserIdAndVoidedFalse(userId);
        Map<String, Object> body = new HashMap<>();
        body.put("count", subs.size());
        body.put("endpoints", subs.stream().map(PushSubscription::getEndpoint).toList());
        return getResponse200(body);
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
