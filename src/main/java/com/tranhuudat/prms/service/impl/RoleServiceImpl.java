package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.role.RoleDto;
import com.tranhuudat.prms.repository.RoleRepository;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends BaseService implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getAll() {
        var roles = roleRepository.findAll().stream()
                .filter(r -> r.getVoided() == null || !r.getVoided())
                .map(RoleDto::new)
                .toList();
        return getResponse200(roles);
    }
}

