package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.request.IntrospectRequest;
import com.tranhuudat.prms.dto.request.LoginRequest;
import com.tranhuudat.prms.dto.request.LogoutRequest;
import com.tranhuudat.prms.dto.request.RefreshRequest;
import com.tranhuudat.prms.dto.response.IntrospectResponse;
import com.tranhuudat.prms.dto.response.TokenResponse;
import com.tranhuudat.prms.entity.RefreshToken;
import com.tranhuudat.prms.entity.Token;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.enums.TokenTypeEnum;
import com.tranhuudat.prms.exception.AppException;
import com.tranhuudat.prms.exception.ErrorCode;
import com.tranhuudat.prms.repository.RefreshTokenRepository;
import com.tranhuudat.prms.repository.TokenRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.security.JwtTokenProvider;
import com.tranhuudat.prms.config.AppProperties;
import io.jsonwebtoken.Claims;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    TokenRepository tokenRepository;
    RefreshTokenRepository refreshTokenRepository;
    JwtTokenProvider jwtTokenProvider;
    PasswordEncoder passwordEncoder;
    AuthenticationManager authenticationManager;
    AppProperties appProperties;

    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        boolean isValid = true;

        try {
            jwtTokenProvider.verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public TokenResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = (User) authentication.getPrincipal();

            // revoke all previous tokens for this user
            var tokens = tokenRepository.findAllByUsernameAndExpiredFalseAndRevokedFalse(user.getUsername());
            tokens.forEach(t -> { t.setExpired(true); t.setRevoked(true); });
            tokenRepository.saveAll(tokens);

            var accessToken = jwtTokenProvider.generateToken(user);
            var refreshToken = jwtTokenProvider.generateRefreshToken(user);

            // persist tokens
            tokenRepository.save(Token.builder()
                    .token(accessToken)
                    .type(TokenTypeEnum.BEARER)
                    .expired(false)
                    .revoked(false)
                    .username(user.getUsername())
                    .build());

            // set refresh token expiration aligned to config
            refreshTokenRepository.save(RefreshToken.builder()
                    .token(refreshToken)
                    .username(user.getUsername())
                    .revoked(false)
                    .expiration(Date.from(Instant.now().plus(appProperties.getRefreshableDuration(), ChronoUnit.SECONDS)))
                    .build());

            return TokenResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .authenticated(true)
                    .build();
        } catch (AuthenticationException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    public void logout(LogoutRequest request) {
        try {
            Claims claims = jwtTokenProvider.verifyToken(request.getToken(), true);
            String tokenStr = request.getToken();

            tokenRepository.findByToken(tokenStr).ifPresent(token -> {
                token.setRevoked(true);
                token.setExpired(true);
                tokenRepository.save(token);
            });

            refreshTokenRepository.findByToken(tokenStr).ifPresent(rt -> {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            });
        } catch (AppException e) {
            // Token already expired or invalid
        }
    }

    public TokenResponse refreshToken(RefreshRequest request) {
        Claims claims = jwtTokenProvider.verifyToken(request.getToken(), true);

        var username = claims.getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // ensure provided token exists and not revoked
        var refreshToken = refreshTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // revoke all previous access tokens
        tokenRepository.findAllByUsernameAndExpiredFalseAndRevokedFalse(user.getUsername())
                .forEach(t -> { t.setExpired(true); t.setRevoked(true); });

        var newAccessToken = jwtTokenProvider.generateToken(user);
        tokenRepository.save(Token.builder()
                .token(newAccessToken)
                .type(TokenTypeEnum.BEARER)
                .expired(false)
                .revoked(false)
                .username(user.getUsername())
                .build());

        return TokenResponse.builder()
                .token(newAccessToken)
                .refreshToken(request.getToken())
                .authenticated(true)
                .build();
    }
}
