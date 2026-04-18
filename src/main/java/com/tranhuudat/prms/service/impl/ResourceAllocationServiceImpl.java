package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.resource_allocation.ResourceAllocationDto;
import com.tranhuudat.prms.dto.resource_allocation.ResourceAllocationSearchRequest;
import com.tranhuudat.prms.entity.ResourceAllocation;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.repository.AppParamRepository;
import com.tranhuudat.prms.repository.ResourceAllocationRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.ResourceAllocationService;
import com.tranhuudat.prms.util.ConstUtil;
import com.tranhuudat.prms.util.DateUtil;
import com.tranhuudat.prms.util.SystemMessage;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceAllocationServiceImpl extends BaseService implements ResourceAllocationService {

    private static final String PARAM_GROUP_MODULE_RESOURCE_ALLOCATION = "MODULE_RESOURCE_ALLOCATION";
    private static final String PARAM_TYPE_RESOURCE_ALLOCATION = "RESOURCE_ALLOCATION";

    private final ResourceAllocationRepository resourceAllocationRepository;
    private final UserRepository userRepository;
    private final AppParamRepository appParamRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public BaseResponse create(ResourceAllocationDto request) {
        HashMap<String, String> errors = validation(request);
        errors.putAll(validateBusiness(request));
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        ResourceAllocation entity = new ResourceAllocation();
        BeanUtils.copyProperties(request, entity, ConstUtil.NON_UPDATABLE_FIELDS);
        entity.setResourceMonth(request.getMonth());
        entity.setVoided(false);
        entity = resourceAllocationRepository.save(entity);
        return getResponse201(new ResourceAllocationDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional
    public BaseResponse update(UUID id, ResourceAllocationDto request) {
        HashMap<String, String> errors = validation(request);
        ResourceAllocation entity = resourceAllocationRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, "resource_allocation"));
        }
        errors.putAll(validateBusiness(request));
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        BeanUtils.copyProperties(request, entity, ConstUtil.NON_UPDATABLE_FIELDS);
        entity.setResourceMonth(request.getMonth());
        entity = resourceAllocationRepository.save(entity);
        return getResponse200(new ResourceAllocationDto(entity));
    }

    @Override
    @Transactional
    public BaseResponse delete(UUID id) {
        ResourceAllocation entity = resourceAllocationRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, "resource_allocation"));
        }
        entity.setVoided(true);
        resourceAllocationRepository.save(entity);
        return getResponse200(new ResourceAllocationDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getById(UUID id) {
        ResourceAllocation entity = resourceAllocationRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, "resource_allocation"));
        }
        return getResponse200(new ResourceAllocationDto(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getPage(ResourceAllocationSearchRequest request) {
        if(Objects.nonNull(request.getMonth())){
            request.setYear(DateUtil.getYear(request.getMonth()));
            request.setMonthYear(DateUtil.getMonth(request.getMonth()));
        }
        return getResponse200(resourceAllocationRepository.getPages(entityManager, request, getPageable(request)));
    }

    private HashMap<String, String> validateBusiness(ResourceAllocationDto request) {
        HashMap<String, String> errors = new HashMap<>();
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null || Boolean.TRUE.equals(user.getVoided())) {
                errors.put("userId", getMessage(SystemMessage.NOT_FOUND, "user"));
            }
        }
        if (StringUtils.hasText(request.getRole())
                && !appParamRepository.existsActiveParamValue(
                PARAM_GROUP_MODULE_RESOURCE_ALLOCATION,
                PARAM_TYPE_RESOURCE_ALLOCATION,
                request.getRole().trim())) {
            errors.put("role", getMessage(SystemMessage.BAD_REQUEST));
        }
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getStartDate().after(request.getEndDate())) {
            errors.put("endDate", getMessage(SystemMessage.BAD_REQUEST));
        }
        return errors;
    }
}
