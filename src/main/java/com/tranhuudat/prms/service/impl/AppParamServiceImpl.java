package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.app_param.AppParamDto;
import com.tranhuudat.prms.dto.app_param.AppParamSearchRequest;
import com.tranhuudat.prms.entity.AppParam;
import com.tranhuudat.prms.repository.AppParamRepository;
import com.tranhuudat.prms.service.AppParamService;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.util.ConstUtil;
import com.tranhuudat.prms.util.SystemMessage;
import com.tranhuudat.prms.util.SystemVariable;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppParamServiceImpl extends BaseService implements AppParamService {
    private final AppParamRepository appParamRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public BaseResponse create(AppParamDto request) {
        HashMap<String, String> errors = validation(request);
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        AppParam entity = new AppParam();
        BeanUtils.copyProperties(request, entity, ConstUtil.NON_UPDATABLE_FIELDS);
        entity.setVoided(false);
        entity = appParamRepository.save(entity);
        return getResponse201(new AppParamDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional
    public BaseResponse update(UUID id, AppParamDto request) {
        HashMap<String, String> errors = validation(request);
        AppParam entity = appParamRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.APP_PARAM)));
        }
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        BeanUtils.copyProperties(request, entity, ConstUtil.NON_UPDATABLE_FIELDS);
        entity = appParamRepository.save(entity);
        return getResponse200(new AppParamDto(entity));
    }

    @Override
    @Transactional
    public BaseResponse delete(UUID id) {
        AppParam entity = appParamRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.APP_PARAM)));
        }
        entity.setVoided(true);
        entity = appParamRepository.save(entity);
        return getResponse200(new AppParamDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getById(UUID id) {
        AppParam entity = appParamRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.APP_PARAM)));
        }
        return getResponse200(new AppParamDto(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getPage(AppParamSearchRequest request) {
        return getResponse200(appParamRepository.getPages(entityManager, request, getPageable(request)));
    }
}
