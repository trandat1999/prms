package com.tranhuudat.prms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
}
