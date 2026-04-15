package com.tranhuudat.prms.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.json.gson.GsonFactory;
import com.tranhuudat.prms.config.AppProperties;
import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.authentication.*;
import com.tranhuudat.prms.entity.RefreshToken;
import com.tranhuudat.prms.entity.Role;
import com.tranhuudat.prms.entity.Token;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.enums.RoleEnum;
import com.tranhuudat.prms.enums.TokenTypeEnum;
import com.tranhuudat.prms.repository.RefreshTokenRepository;
import com.tranhuudat.prms.repository.RoleRepository;
import com.tranhuudat.prms.repository.TokenRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.service.AuthenticationService;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.JwtService;
import com.tranhuudat.prms.util.ConstUtil;
import com.tranhuudat.prms.util.SystemMessage;
import com.tranhuudat.prms.util.SystemVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * @author DatNuclear 04/05/2026 04:02 PM
 * @project prms
 * @package com.tranhuudat.prms.service.impl
 */
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl extends BaseService implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties appProperties;

    @Override
    public BaseResponse login(AuthRequest request) {
        var validations = validation(request);
        if (!CollectionUtils.isEmpty(validations)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), validations);
        }
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),
                request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        var user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        var token = jwtService.generateToken(request.getUsername());
        var refreshToken = UUID.randomUUID().toString();
        revokeAllTokenAndRefreshToken(user.getUsername(),true);
        saveToken(user.getUsername(), token);
        saveRefreshToken(user.getUsername(), refreshToken);
        AuthResponse authResponse = AuthResponse.builder()
                .refreshToken(refreshToken)
                .accessToken(token).build();
        return getResponse200(authResponse, getMessage(SystemMessage.SUCCESS));
    }

    @Override
    public BaseResponse register(RegisterRequest request) {
        HashMap<String, String> validations = validation(request);
        boolean checkExistUsername = userRepository.existsByUsername(request.getUsername());
        if (checkExistUsername) {
            validations.put(SystemVariable.USERNAME, getMessage(SystemMessage.VALUE_EXIST, request.getUsername()));
        }
        boolean checkExistEmail = userRepository.existsByEmail(request.getEmail());
        if (checkExistEmail) {
            validations.put(SystemVariable.EMAIL, getMessage(SystemMessage.VALUE_EXIST, request.getEmail()));
        }
        if (!CollectionUtils.isEmpty(validations)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), validations);
        }
        Role userRole = roleRepository.findByCode(RoleEnum.USER.getCode()).orElse(Role.builder().name(ConstUtil.USER_ROLE).build());
        User user = User.builder().username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .enabled(false)
                .accountNonExpired(false)
                .credentialsNonExpired(false)
                .accountNonLocked(false)
                .roles(Set.of(userRole))
                .build();
        user = userRepository.save(user);
//        mailService.sendMailActive(user);
        return getResponse200(true, getMessage(SystemMessage.REGISTER_SUCCESS));
    }

    @Override
    public BaseResponse verifyAccount(String token) {
        return null;
    }

    @Override
    public BaseResponse generateActiveToken(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            HashMap<String, String> validations = new HashMap<>();
            validations.put(SystemVariable.USERNAME, getMessage(SystemMessage.NOT_FOUND, username));
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), validations);
        }
        User user = userOptional.get();
        if (user.isAccountNonLocked() && user.getAccountNonExpired()
                && user.getCredentialsNonExpired() && user.getEnabled()) {
            HashMap<String, String> validations = new HashMap<>();
            validations.put(SystemVariable.USERNAME, getMessage(SystemMessage.ACTIVATED, username));
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), validations);
        }
//        mailService.sendMailActive(user);
        return getResponse200(true, getMessage(SystemMessage.GENERATE_TOKEN_SUCCESS));
    }

    @Override
    public BaseResponse forgotPassword(ForgotPasswordRequest request) {
        return null;
    }

    @Override
    public BaseResponse activeNewPassword(String token) {
        return null;
    }

    @Override
    public BaseResponse generateNewToken(String token) {
        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findByToken(token);
        if(refreshTokenOptional.isEmpty() || refreshTokenOptional.get().getRevoked()){
            return getResponse400(getMessage(SystemMessage.TOKEN_INVALID));
        }
        RefreshToken refreshToken = refreshTokenOptional.get();
        if(refreshToken.getExpiration().before(new Date())){
            return getResponse400(getMessage(SystemMessage.TOKEN_EXPIRED));
        }
        String jwtToken = jwtService.generateToken(refreshToken.getUsername());
//        String refreshTokenString = UUID.randomUUID().toString();
        revokeAllTokenAndRefreshToken(refreshToken.getUsername(),false);
        saveToken(refreshToken.getUsername(), jwtToken);
//        saveRefreshToken(refreshToken.getUsername(), refreshTokenString);
        AuthResponse authResponse = AuthResponse.builder()
                .refreshToken(token)
                .accessToken(jwtToken).build();
        return getResponse200(authResponse, getMessage(SystemMessage.SUCCESS));
    }

    @Override
    public BaseResponse refreshToken(String token) {
        return null;
    }

    @Override
    public BaseResponse loginGoogle(OAuthRequest request) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken =GoogleIdToken.parse(GsonFactory.getDefaultInstance(), request.getIdToken());
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        Optional<User> userOptional = userRepository.findByUsername(email);
        if(userOptional.isEmpty()){
            Role role = roleRepository.findByName(ConstUtil.USER_ROLE).orElse(Role.builder().name(ConstUtil.USER_ROLE).build());
            var user = User.builder()
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .accountNonExpired(true)
                    .enabled(true)
                    .email(email)
                    .password(passwordEncoder.encode(ConstUtil.ADMIN_PASSWORD))
                    .username(email)
                    .fullName(payload.get("name").toString())
                    .roles(Set.of(role))
                    .build();
            userRepository.save(user);
        }else{
            var user = userOptional.get();
            userRepository.save(user);
        }
        var token = jwtService.generateToken(email);
        var refreshToken = UUID.randomUUID().toString();
        revokeAllTokenAndRefreshToken(email,true);
        saveToken(email, token);
        saveRefreshToken(email, refreshToken);
        AuthResponse authResponse = AuthResponse.builder()
                .refreshToken(refreshToken)
                .accessToken(token).build();
        return getResponse200(authResponse, getMessage(SystemMessage.SUCCESS));
    }

    private void saveToken(String username, String jwtToken) {
        var entity = tokenRepository.findByToken(jwtToken).orElse(null);
        if (entity != null) {
            return;
        }
        var token = Token.builder()
                .username(username)
                .token(jwtToken)
                .type(TokenTypeEnum.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void saveRefreshToken(String username, String jwtToken) {
        var token = RefreshToken.builder()
                .username(username)
                .token(jwtToken)
                .expiration(new Date(System.currentTimeMillis() + appProperties.getExpirationRefreshToken()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(token);
    }

    private void revokeAllTokenAndRefreshToken(String username, boolean revokeRefreshToken) {
        List<Token> tokens = tokenRepository.findAllByUsernameAndExpiredFalseAndRevokedFalse(username);
        if (!CollectionUtils.isEmpty(tokens)) {
            tokens.forEach(token -> {
                token.setExpired(true);
                token.setRevoked(true);
            });
            tokenRepository.saveAll(tokens);
        }
        if(revokeRefreshToken){
            List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUsernameAndRevokedFalse(username);
            if (!CollectionUtils.isEmpty(refreshTokens)) {
                refreshTokens.forEach(token -> {
                    token.setRevoked(true);
                });
                refreshTokenRepository.saveAll(refreshTokens);
            }
        }
    }
}
