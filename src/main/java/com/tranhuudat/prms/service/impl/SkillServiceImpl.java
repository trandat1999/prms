package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.skill.SkillDto;
import com.tranhuudat.prms.dto.skill.SkillSearchRequest;
import com.tranhuudat.prms.entity.Skill;
import com.tranhuudat.prms.repository.SkillRepository;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.SkillService;
import com.tranhuudat.prms.util.ConstUtil;
import com.tranhuudat.prms.util.SystemMessage;
import com.tranhuudat.prms.util.SystemVariable;
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
public class SkillServiceImpl extends BaseService implements SkillService {
    private final SkillRepository skillRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public BaseResponse create(SkillDto request) {
        HashMap<String, String> errors = validation(request);
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        if (StringUtils.hasText(request.getCode()) && skillRepository.existsByCodeIgnoreCase(request.getCode().trim())) {
            return getResponse400(
                    getMessage(SystemMessage.VALUE_EXIST, getMessage(SystemVariable.CODE)),
                    new HashMap<>() {{
                        put(SystemVariable.CODE, getMessage(SystemMessage.VALUE_EXIST, getMessage(SystemVariable.CODE)));
                    }});
        }
        Skill entity = new Skill();
        BeanUtils.copyProperties(request, entity, ConstUtil.NON_UPDATABLE_FIELDS);
        entity.setVoided(false);
        entity = skillRepository.save(entity);
        return getResponse201(new SkillDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional
    public BaseResponse update(UUID id, SkillDto request) {
        HashMap<String, String> errors = validation(request);
        Skill entity = skillRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.SKILL)));
        }
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        if (StringUtils.hasText(request.getCode())
                && !request.getCode().trim().equalsIgnoreCase(entity.getCode())
                && skillRepository.existsByCodeIgnoreCase(request.getCode().trim())) {
            HashMap<String, String> exist = new HashMap<>();
            exist.put(SystemVariable.CODE, getMessage(SystemMessage.VALUE_EXIST, getMessage(SystemVariable.CODE)));
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), exist);
        }
        BeanUtils.copyProperties(request, entity, ConstUtil.NON_UPDATABLE_FIELDS);
        entity = skillRepository.save(entity);
        return getResponse200(new SkillDto(entity));
    }

    @Override
    @Transactional
    public BaseResponse delete(UUID id) {
        Skill entity = skillRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.SKILL)));
        }
        entity.setVoided(true);
        entity = skillRepository.save(entity);
        return getResponse200(new SkillDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getById(UUID id) {
        Skill entity = skillRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.SKILL)));
        }
        return getResponse200(new SkillDto(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getPage(SkillSearchRequest request) {
        return getResponse200(skillRepository.getPages(entityManager, request, getPageable(request)));
    }
}

