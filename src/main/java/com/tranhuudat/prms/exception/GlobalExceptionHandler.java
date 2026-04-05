package com.tranhuudat.prms.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.util.SystemMessage;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private final MessageSource messageSource;

    @ExceptionHandler({CredentialsExpiredException.class, BadCredentialsException.class,
            AccessDeniedException.class, AuthenticationException.class,
            UsernameNotFoundException.class, SignatureException.class, ExpiredJwtException.class})
    public ResponseEntity<BaseResponse> handleSecurityException(Exception exception) {
        BaseResponse errorDetail = BaseResponse.builder()
                .timestamp(LocalDateTime.now())
                .build();
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
        log.error(exception.getMessage());
        if (exception instanceof CredentialsExpiredException) {
            errorDetail.setMessage(messageSource.getMessage(SystemMessage.USER_CREDENTIALS_IS_EXPIRED, null, LocaleContextHolder.getLocale()));
        }
        if (exception instanceof LockedException) {
            errorDetail.setMessage(messageSource.getMessage(SystemMessage.USER_IS_LOCKED, null, LocaleContextHolder.getLocale()));
        }
        if (exception instanceof AccountExpiredException) {
            errorDetail.setMessage(messageSource.getMessage(SystemMessage.USER_IS_EXPIRED, null, LocaleContextHolder.getLocale()));
        }
        if (exception instanceof DisabledException) {
            errorDetail.setMessage(messageSource.getMessage(SystemMessage.USER_IS_DISABLE, null, LocaleContextHolder.getLocale()));
        }
        if (exception instanceof BadCredentialsException) {
            httpStatus = HttpStatus.BAD_REQUEST;
            errorDetail.setMessage(messageSource.getMessage(SystemMessage.USER_BAD_CREDENTIALS, null, LocaleContextHolder.getLocale()));
        }
        if (exception instanceof AccessDeniedException) {
            httpStatus = HttpStatus.FORBIDDEN;
            errorDetail.setMessage(messageSource.getMessage(SystemMessage.ACCESS_DENIED, null, LocaleContextHolder.getLocale()));
        }
        if (exception instanceof ExpiredJwtException) {
            errorDetail.setMessage(messageSource.getMessage(SystemMessage.JWT_IS_EXPIRED, null, LocaleContextHolder.getLocale()));
        }
        if (exception instanceof SignatureException) {
            errorDetail.setMessage(messageSource.getMessage(SystemMessage.TOKEN_INVALID, null, LocaleContextHolder.getLocale()));
        }
        errorDetail.setCode(httpStatus.value());
        errorDetail.setStatus(httpStatus.name());
        return ResponseEntity.status(httpStatus).body(errorDetail);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        BaseResponse errorDetail = BaseResponse.builder()
                .timestamp(LocalDateTime.now())
                .build();
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        errorDetail.setCode(httpStatus.value());
        errorDetail.setStatus(httpStatus.name());
        errorDetail.setBody("Invalid value. Please provide one of the valid values.");;
        if (ex.getCause() instanceof InvalidFormatException cause) {
            Class<?> targetType = cause.getTargetType();
            if (targetType.isEnum()) {
                Map<String,String> rs = new HashMap<>();
                String[] validValues = Arrays.stream(targetType.getEnumConstants())
                        .map(Object::toString)
                        .toArray(String[]::new);
                rs.put(cause.getPath().get(0).getFieldName(),String.format("Invalid value for field '%s'. Allowed values are: %s",
                        cause.getPath().get(0).getFieldName(),
                        String.join(", ", validValues)));
                errorDetail.setBody(rs);
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetail);
    }

    @ExceptionHandler({Exception.class,AppException.class})
    public final ResponseEntity<BaseResponse> handleAllExceptions(Exception ex, WebRequest request) {
        log.error(ex.getMessage(),ex);
        BaseResponse error = BaseResponse.builder()
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
