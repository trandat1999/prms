package com.tranhuudat.prms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.util.SystemMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

import static com.tranhuudat.prms.util.SystemMessage.FORBIDDEN;

/**
 * @author DatNuclear 04/05/2026 04:53 PM
 * @project prms
 * @package com.tranhuudat.prms.config
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final MessageSource messageSource;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse httpServletResponse, AuthenticationException authException) throws IOException, ServletException {
        if (httpServletResponse.getStatus() == HttpStatus.OK.value()) {
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            BaseResponse errorResponse = BaseResponse.builder()
                    .status(HttpStatus.UNAUTHORIZED.name())
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message(messageSource.getMessage(SystemMessage.UNAUTHORIZED, null, LocaleContextHolder.getLocale()))
                    .build();
            OutputStream outputStream = httpServletResponse.getOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(outputStream, errorResponse);
            outputStream.flush();
        }
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse httpServletResponse, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setStatus(403);
        BaseResponse errorResponse = BaseResponse.builder()
                .status(HttpStatus.FORBIDDEN.name())
                .code(HttpStatus.UNAUTHORIZED.value())
                .message(messageSource.getMessage(FORBIDDEN,null, LocaleContextHolder.getLocale()))
                .build();
        OutputStream outputStream = httpServletResponse.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(outputStream, errorResponse);
        outputStream.flush();
    }
}
