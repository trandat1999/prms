package com.tranhuudat.prms.controller;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.notification.UserNotificationSearchRequest;
import com.tranhuudat.prms.service.UserNotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserNotificationController {

    UserNotificationService userNotificationService;

    @PostMapping("/page")
    public ResponseEntity<BaseResponse> getPage(@RequestBody UserNotificationSearchRequest request) {
        return ResponseEntity.ok(userNotificationService.getPage(request));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<BaseResponse> unreadCount() {
        return ResponseEntity.ok(userNotificationService.unreadCount());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<BaseResponse> markRead(@PathVariable UUID id) {
        return ResponseEntity.ok(userNotificationService.markRead(id));
    }
}
