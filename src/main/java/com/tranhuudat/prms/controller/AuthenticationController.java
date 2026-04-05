package com.tranhuudat.prms.controller;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.authentication.AuthRequest;
import com.tranhuudat.prms.dto.authentication.ForgotPasswordRequest;
import com.tranhuudat.prms.dto.authentication.OAuthRequest;
import com.tranhuudat.prms.dto.authentication.RegisterRequest;
import com.tranhuudat.prms.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<BaseResponse> login(@RequestBody AuthRequest request){
        return ResponseEntity.ok(authenticationService.login(request));
    }
    @PostMapping("/register")
    public ResponseEntity<BaseResponse> register(@RequestBody RegisterRequest request){
        return ResponseEntity.ok(authenticationService.register(request));
    }
    @GetMapping("/verification/{token}")
    public ResponseEntity<BaseResponse> verifyAccount(@PathVariable("token") String token){
        return ResponseEntity.ok(authenticationService.verifyAccount(token));
    }
    @GetMapping("/generation-active-token/{username}")
    public ResponseEntity<BaseResponse> generateActiveToken(@PathVariable("username") String username){
        return ResponseEntity.ok(authenticationService.generateActiveToken(username));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<BaseResponse> register(@RequestBody ForgotPasswordRequest request){
        return ResponseEntity.ok(authenticationService.forgotPassword(request));
    }
    @GetMapping("/forgot-password/{token}")
    public ResponseEntity<BaseResponse> activePassword(@PathVariable("token") String token) {
        return ResponseEntity.ok(authenticationService.activeNewPassword(token));
    }
    @GetMapping("/forgot-password/new-token/{token}")
    public ResponseEntity<BaseResponse> generateNewToken(@PathVariable("token") String token) {
        return ResponseEntity.ok(authenticationService.generateNewToken(token));
    }
    @GetMapping("/refresh-token/{token}")
    public ResponseEntity<BaseResponse> refreshToken(@PathVariable("token") String token) {
        return ResponseEntity.ok(authenticationService.refreshToken(token));
    }
    @PostMapping("/login-google")
    public BaseResponse loginWithGoogle(@RequestBody OAuthRequest request) throws GeneralSecurityException, IOException {
        return authenticationService.loginGoogle(request);
    }
}
