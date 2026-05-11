package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.skill.SkillDto;
import com.tranhuudat.prms.dto.skill.SkillSearchRequest;

import java.util.UUID;

public interface SkillService {
    BaseResponse create(SkillDto request);

    BaseResponse update(UUID id, SkillDto request);

    BaseResponse delete(UUID id);

    BaseResponse getById(UUID id);

    BaseResponse getPage(SkillSearchRequest request);
}

