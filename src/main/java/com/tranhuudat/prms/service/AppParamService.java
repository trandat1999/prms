package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.app_param.AppParamDto;
import com.tranhuudat.prms.dto.app_param.AppParamSearchRequest;

import java.util.UUID;

public interface AppParamService {
    BaseResponse create(AppParamDto request);
    BaseResponse update(UUID id, AppParamDto request);
    BaseResponse delete(UUID id);
    BaseResponse getById(UUID id);
    BaseResponse getPage(AppParamSearchRequest request);
}

