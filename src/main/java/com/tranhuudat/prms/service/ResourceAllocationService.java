package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.resource_allocation.ResourceAllocationDto;
import com.tranhuudat.prms.dto.resource_allocation.ResourceAllocationSearchRequest;

import java.util.UUID;

public interface ResourceAllocationService {

    BaseResponse create(ResourceAllocationDto request);

    BaseResponse update(UUID id, ResourceAllocationDto request);

    BaseResponse delete(UUID id);

    BaseResponse getById(UUID id);

    BaseResponse getPage(ResourceAllocationSearchRequest request);
}
