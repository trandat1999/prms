package com.tranhuudat.prms.dto.notification;

import lombok.Data;

@Data
public class PushSubscriptionUpsertRequest {
    private String endpoint;
    private Long expirationTime;
    private Keys keys;

    @Data
    public static class Keys {
        private String p256dh;
        private String auth;
    }
}

