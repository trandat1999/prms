package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.project_member.ProjectMemberDto;
import com.tranhuudat.prms.dto.project_member.ProjectMemberSearchRequest;
import com.tranhuudat.prms.entity.Project;
import com.tranhuudat.prms.entity.ProjectMember;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.repository.ProjectMemberRepository;
import com.tranhuudat.prms.repository.ProjectRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.ProjectMemberService;
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
public class ProjectMemberServiceImpl extends BaseService implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public BaseResponse create(ProjectMemberDto request) {
        HashMap<String, String> errors = validation(request);
        errors.putAll(validateBusiness(request, null));
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        ProjectMember entity = new ProjectMember();
        BeanUtils.copyProperties(request, entity, ConstUtil.NON_UPDATABLE_FIELDS);
        entity.setVoided(false);
        if (entity.getActive() == null) {
            entity.setActive(true);
        }
        if (entity.getIsLead() == null) {
            entity.setIsLead(false);
        }
        entity = projectMemberRepository.save(entity);
        return getResponse201(new ProjectMemberDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional
    public BaseResponse update(UUID id, ProjectMemberDto request) {
        HashMap<String, String> errors = validation(request);
        ProjectMember entity = projectMemberRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.PROJECT_MEMBER)));
        }
        errors.putAll(validateBusiness(request, id));
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        BeanUtils.copyProperties(request, entity, ConstUtil.NON_UPDATABLE_FIELDS);
        if (entity.getActive() == null) {
            entity.setActive(true);
        }
        if (entity.getIsLead() == null) {
            entity.setIsLead(false);
        }
        entity = projectMemberRepository.save(entity);
        return getResponse200(new ProjectMemberDto(entity));
    }

    @Override
    @Transactional
    public BaseResponse delete(UUID id) {
        ProjectMember entity = projectMemberRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.PROJECT_MEMBER)));
        }
        entity.setVoided(true);
        entity = projectMemberRepository.save(entity);
        return getResponse200(new ProjectMemberDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getById(UUID id) {
        ProjectMember entity = projectMemberRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.PROJECT_MEMBER)));
        }
        return getResponse200(new ProjectMemberDto(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getPage(ProjectMemberSearchRequest request) {
        return getResponse200(projectMemberRepository.getPages(entityManager, request, getPageable(request)));
    }

    private HashMap<String, String> validateBusiness(ProjectMemberDto request, UUID id) {
        HashMap<String, String> errors = new HashMap<>();
        Project project = request.getProjectId() != null ? projectRepository.findById(request.getProjectId()).orElse(null) : null;
        if (request.getProjectId() != null && (project == null || Boolean.TRUE.equals(project.getVoided()))) {
            errors.put(SystemVariable.PROJECT_ID, getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.PROJECT)));
        }
        User user = request.getUserId() != null ? userRepository.findById(request.getUserId()).orElse(null) : null;
        if (request.getUserId() != null && (user == null || Boolean.TRUE.equals(user.getVoided()))) {
            errors.put(SystemVariable.USER_ID, getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.USER)));
        }
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getStartDate().after(request.getEndDate())) {
            errors.put(SystemVariable.END_DATE, getMessage(SystemMessage.BAD_REQUEST));
        }
        if (request.getProjectId() != null && request.getUserId() != null) {
            boolean duplicated = id == null
                    ? projectMemberRepository.existsByProjectIdAndUserIdAndVoidedFalse(request.getProjectId(), request.getUserId())
                    : projectMemberRepository.existsByProjectIdAndUserIdAndIdNotAndVoidedFalse(
                    request.getProjectId(), request.getUserId(), id);
            if (duplicated) {
                errors.put(SystemVariable.USER_ID, getMessage(SystemMessage.BAD_REQUEST));
            }
        }
        return errors;
    }
}
