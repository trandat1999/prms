package com.tranhuudat.prms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class AppProperties {
    @Value("${app.jwt.secret-key}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-access-token}")
    private long expirationAccessToken;

    @Value("${app.jwt.expiration-refresh-token}")
    private long expirationRefreshToken;

    @Value("${app.push.vapid-public-key:}")
    private String pushVapidPublicKey;

    @Value("${app.push.vapid-private-key:}")
    private String pushVapidPrivateKey;

    @Value("${app.push.subject:mailto:admin@localhost}")
    private String pushSubject;

    /**
     * Base URL của frontend (để build link trong email/notification). Ví dụ: http://localhost:4200
     */
    @Value("${app.client.base-url:http://localhost:4200}")
    private String clientBaseUrl;

    /**
     * Base URL của backend (nếu cần build link public file/api). Ví dụ: http://localhost:9999
     */
    @Value("${app.server.base-url:http://localhost:9999}")
    private String serverBaseUrl;
}
