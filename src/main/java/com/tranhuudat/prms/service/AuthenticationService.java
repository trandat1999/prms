package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.request.IntrospectRequest;
import com.tranhuudat.prms.dto.request.LoginRequest;
import com.tranhuudat.prms.dto.request.LogoutRequest;
import com.tranhuudat.prms.dto.request.RefreshRequest;
import com.tranhuudat.prms.dto.response.IntrospectResponse;
import com.tranhuudat.prms.dto.response.TokenResponse;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.exception.AppException;
import com.tranhuudat.prms.exception.ErrorCode;
import com.tranhuudat.prms.repository.InvalidatedTokenRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.security.JwtTokenProvider;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    JwtTokenProvider jwtTokenProvider;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;
    AuthenticationManager authenticationManager;

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
            var token = jwtTokenProvider.generateToken(user);

            return TokenResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .build();
        } catch (AuthenticationException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    public void logout(LogoutRequest request) {
        try {
            Claims claims = jwtTokenProvider.verifyToken(request.getToken(), true);

            String jit = claims.getId();
            var expiryTime = claims.getExpiration();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            // Token already expired or invalid
        }
    }

    public TokenResponse refreshToken(RefreshRequest request) {
        Claims claims = jwtTokenProvider.verifyToken(request.getToken(), true);

        String jit = claims.getId();
        var expiryTime = claims.getExpiration();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);

        var username = claims.getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = jwtTokenProvider.generateToken(user);

        return TokenResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }
}
