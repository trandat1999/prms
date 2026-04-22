package com.tranhuudat.prms.controller;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.notification.PushSubscriptionDeleteRequest;
import com.tranhuudat.prms.dto.notification.PushSubscriptionUpsertRequest;
import com.tranhuudat.prms.dto.notification.UserNotificationDto;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.service.UserPushSubscriptionService;
import com.tranhuudat.prms.service.UserWebPushService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications/push")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserPushSubscriptionController {
    UserPushSubscriptionService userPushSubscriptionService;
    UserWebPushService userWebPushService;
    UserRepository userRepository;

    @PostMapping("/subscribe")
    public ResponseEntity<BaseResponse> subscribe(@RequestBody PushSubscriptionUpsertRequest request) {
        return ResponseEntity.ok(userPushSubscriptionService.upsert(request));
    }

    @DeleteMapping("/unsubscribe")
    public ResponseEntity<BaseResponse> unsubscribe(@RequestBody PushSubscriptionDeleteRequest request) {
        return ResponseEntity.ok(userPushSubscriptionService.delete(request));
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse> me() {
        return ResponseEntity.ok(userPushSubscriptionService.mySubscriptions());
    }

    /**
     * Endpoint test nhanh: đóng tab/ẩn tab rồi gọi API này để kiểm tra Web Push có hiện notification OS không.
     */
    @PostMapping("/test")
    public ResponseEntity<BaseResponse> testPush() {
        UUID userId = resolveCurrentUserId();
        if (userId != null) {
            UserNotificationDto dto = new UserNotificationDto();
            dto.setMessage("Test Web Push từ PRMS");
            userWebPushService.push(userId, dto);
        }
        return ResponseEntity.ok(BaseResponse.builder()
                .code(200)
                .status("OK")
                .message("Success.")
                .build());
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

