package com.tranhuudat.prms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class AppProperties {
    private String signerKey;
    private long validDuration;
    private long refreshableDuration;
}
