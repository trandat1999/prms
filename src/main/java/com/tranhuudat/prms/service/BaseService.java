package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.SearchRequest;
import com.tranhuudat.prms.dto.response.PageResponse;
import com.tranhuudat.prms.util.SystemMessage;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public abstract class BaseService{
    @Resource
    private Validator validator;
    @Resource
    private MessageSource messageSource;

    protected BaseResponse getResponse404(String message) {
        return BaseResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(HttpStatus.NOT_FOUND.value())
                .status(HttpStatus.NOT_FOUND.name())
                .message(message)
                .build();
    }

    protected BaseResponse getResponse200(Object object, String message) {
        return BaseResponse.builder()
                .timestamp(LocalDateTime.now())
                .body(object)
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK.name())
                .message(message)
                .build();
    }

    protected BaseResponse getResponse200(Object object) {
        return getResponse200(object, SystemMessage.SUCCESS);
    }

    protected BaseResponse getResponse204(Object object, String message) {
        return BaseResponse.builder()
                .timestamp(LocalDateTime.now())
                .body(object)
                .code(HttpStatus.NO_CONTENT.value())
                .status(HttpStatus.NO_CONTENT.name())
                .message(message)
                .build();
    }

    protected BaseResponse getResponse201(Object object, String message) {
        return BaseResponse.builder()
                .timestamp(LocalDateTime.now())
                .body(object)
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED.name())
                .message(message)
                .build();
    }

    protected BaseResponse getResponse400(String message, Object object) {
        return BaseResponse.builder()
                .timestamp(LocalDateTime.now())
                .body(object)
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST.name())
                .message(message)
                .build();
    }

    protected BaseResponse getResponse400(String message) {
        return BaseResponse.builder()
                .timestamp(LocalDateTime.now())
                .body(null)
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST.name())
                .message(message)
                .build();
    }

    protected BaseResponse getResponse500(String message) {
        return BaseResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .message(message)
                .build();
    }

    protected String getMessage(String message, Object... objects) {
        return messageSource.getMessage(message, objects, LocaleContextHolder.getLocale());
    }

    protected Pageable getPageable(Object object) {
        SearchRequest searchRequest = (SearchRequest) object;
        int pageIndex = 0;
        int pageSize = 10;
        if (!ObjectUtils.isEmpty(searchRequest.getPageIndex()) && searchRequest.getPageIndex() >= 0) {
            pageIndex = searchRequest.getPageIndex();
        }
        if (!ObjectUtils.isEmpty(searchRequest.getPageSize()) && searchRequest.getPageSize() > 0) {
            pageSize = searchRequest.getPageSize();
        }
        return PageRequest.of(pageIndex, pageSize);
    }

    protected HashMap<String, String> validation(Object object) {
        HashMap<String, String> rs = new HashMap<>();
        Set<ConstraintViolation<Object>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Object> constraintViolation : violations) {
                rs.put(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage());
            }
        }
        return rs;
    }

    protected  PageResponse toPageResponse(Page<?> pageData, List<?> content) {
        return PageResponse.builder()
                .currentPage(pageData.getNumber() + 1)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(content)
                .build();
    }
}
