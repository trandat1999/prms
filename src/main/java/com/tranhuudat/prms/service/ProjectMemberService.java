package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.project_member.ProjectMemberDto;
import com.tranhuudat.prms.dto.project_member.ProjectMemberSearchRequest;

import java.util.UUID;

public interface ProjectMemberService {

    BaseResponse create(ProjectMemberDto request);

    BaseResponse update(UUID id, ProjectMemberDto request);

    BaseResponse delete(UUID id);

    BaseResponse getById(UUID id);

    BaseResponse getPage(ProjectMemberSearchRequest request);
}
