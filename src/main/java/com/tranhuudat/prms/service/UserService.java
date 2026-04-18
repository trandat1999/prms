package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.user.UserCreateRequest;
import com.tranhuudat.prms.dto.user.UserPasswordUpdateRequest;
import com.tranhuudat.prms.dto.user.UserSearchRequest;
import com.tranhuudat.prms.dto.user.UserUpdateRequest;

import java.util.UUID;

/**
 * @author DatNuclear 04/05/2026 09:00 PM
 * @project prms
 * @package com.tranhuudat.prms.service
 */
public interface UserService {
    BaseResponse getCurrentUser();
    BaseResponse getById(UUID id);
    BaseResponse getPage(UserSearchRequest request);
    BaseResponse create(UserCreateRequest request);
    BaseResponse update(UUID id, UserUpdateRequest request);
    BaseResponse delete(UUID id);
    BaseResponse updatePassword(UUID id, UserPasswordUpdateRequest request);
}
