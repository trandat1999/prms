package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.authentication.AuthRequest;
import com.tranhuudat.prms.dto.authentication.ForgotPasswordRequest;
import com.tranhuudat.prms.dto.authentication.OAuthRequest;
import com.tranhuudat.prms.dto.authentication.RegisterRequest;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface AuthenticationService {
    BaseResponse login(AuthRequest request);
    BaseResponse register(RegisterRequest request);
    BaseResponse verifyAccount(String token);
    BaseResponse generateActiveToken(String username);
    BaseResponse forgotPassword(ForgotPasswordRequest request);
    BaseResponse activeNewPassword(String token);
    BaseResponse generateNewToken(String token);
    BaseResponse refreshToken(String token);
    BaseResponse loginGoogle(OAuthRequest request) throws GeneralSecurityException, IOException;
}
