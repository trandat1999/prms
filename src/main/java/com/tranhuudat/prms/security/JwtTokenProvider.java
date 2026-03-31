package com.tranhuudat.prms.security;

import com.tranhuudat.prms.config.AppProperties;
import com.tranhuudat.prms.exception.AppException;
import com.tranhuudat.prms.exception.ErrorCode;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.repository.InvalidatedTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtTokenProvider {
    AppProperties appProperties;
    InvalidatedTokenRepository invalidatedTokenRepository;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(appProperties.getSignerKey().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .issuer("tranhuudat.com")
                .issuedAt(new Date())
                .expiration(new Date(
                        Instant.now().plus(appProperties.getValidDuration(), ChronoUnit.SECONDS).toEpochMilli()
                ))
                .id(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .signWith(getSigningKey())
                .compact();
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> stringJoiner.add(role.getName()));
        }
        return stringJoiner.toString();
    }

    public Claims verifyToken(String token, boolean isRefresh) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (invalidatedTokenRepository.existsById(claims.getId())) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            Date expiryTime = (isRefresh)
                    ? new Date(claims.getIssuedAt().toInstant()
                        .plus(appProperties.getRefreshableDuration(), ChronoUnit.SECONDS).toEpochMilli())
                    : claims.getExpiration();

            if (expiryTime.before(new Date())) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            return claims;
        } catch (Exception e) {
            log.error("Token verification failed", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }
}
