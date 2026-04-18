package com.tranhuudat.prms.service.impl;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.project.ProjectDTO;
import com.tranhuudat.prms.dto.project.ProjectSearchRequest;
import com.tranhuudat.prms.entity.Project;
import com.tranhuudat.prms.repository.ProjectRepository;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.ProjectService;
import com.tranhuudat.prms.util.ConstUtil;
import com.tranhuudat.prms.util.SystemMessage;
import com.tranhuudat.prms.util.SystemVariable;
import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl extends BaseService implements ProjectService {

    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public BaseResponse create(ProjectDTO request) {
        HashMap<String, String> errors = validation(request);
        if (projectRepository.existsByCode(request.getCode())) {
            errors.put(SystemVariable.CODE, getMessage(SystemMessage.VALUE_EXIST, request.getCode()));
        }
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        Project entity = new Project();
        BeanUtils.copyProperties(request, entity);
        entity = projectRepository.save(entity);
        return getResponse201(new ProjectDTO(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional
    public BaseResponse update(UUID id, ProjectDTO request) {
        HashMap<String, String> errors = validation(request);
        Project entity = projectRepository.findById(id).orElse(null);
        if (Objects.isNull(entity)) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.PROJECT)));
        }
        if (projectRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            errors.put(SystemVariable.CODE, getMessage(SystemMessage.VALUE_EXIST, request.getCode()));
        }
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        BeanUtils.copyProperties(request, entity, ConstUtil.NON_UPDATABLE_FIELDS);
        entity = projectRepository.save(entity);
        return getResponse200(new ProjectDTO(entity));
    }

    @Override
    @Transactional
    public BaseResponse delete(UUID id) {
        Project entity = projectRepository.findById(id).orElse(null);
        if (Objects.isNull(entity)) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.PROJECT)));
        }
        entity.setVoided(true);
        entity = projectRepository.save(entity);
        return getResponse200(new ProjectDTO(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getById(UUID id) {
        Project entity = projectRepository.findById(id).orElse(null);
        if (Objects.isNull(entity)) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.PROJECT)));
        }
        return getResponse200(new ProjectDTO(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getPage(ProjectSearchRequest request) {
        return getResponse200(projectRepository.getPages(entityManager, request, getPageable(request)));
    }
}
