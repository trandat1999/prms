package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.request.ProjectRequest;
import com.tranhuudat.prms.dto.request.search.ProjectSearchRequest;

import java.util.UUID;

public interface ProjectService {

    BaseResponse create(ProjectRequest request);

    BaseResponse update(UUID id, ProjectRequest request);

    BaseResponse delete(UUID id);

    BaseResponse getById(UUID id);

    BaseResponse getPage(ProjectSearchRequest request);
}
