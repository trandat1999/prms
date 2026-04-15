package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.project.ProjectDTO;
import com.tranhuudat.prms.dto.project.ProjectSearchRequest;

import java.util.UUID;

public interface ProjectService {

    BaseResponse create(ProjectDTO request);

    BaseResponse update(UUID id, ProjectDTO request);

    BaseResponse delete(UUID id);

    BaseResponse getById(UUID id);

    BaseResponse getPage(ProjectSearchRequest request);
}
