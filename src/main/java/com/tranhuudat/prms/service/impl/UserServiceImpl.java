package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.user.UserDto;
import com.tranhuudat.prms.dto.user.UserCreateRequest;
import com.tranhuudat.prms.dto.user.UserDetailDto;
import com.tranhuudat.prms.dto.user.UserPasswordUpdateRequest;
import com.tranhuudat.prms.dto.user.UserSearchRequest;
import com.tranhuudat.prms.dto.user.UserUpdateRequest;
import com.tranhuudat.prms.entity.Role;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.enums.RoleEnum;
import com.tranhuudat.prms.repository.RoleRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.UserService;
import com.tranhuudat.prms.util.ConstUtil;
import com.tranhuudat.prms.util.SystemMessage;
import com.tranhuudat.prms.util.SystemVariable;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author DatNuclear 04/05/2026 09:03 PM
 * @project prms
 * @package com.tranhuudat.prms.service.impl
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseService implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String username = authentication.getName();
            Optional<User> userOptional = userRepository.findByUsername(username);
            if(userOptional.isEmpty()){
                return getResponse400(null);
            }
            return getResponse200(new UserDto(userOptional.get()));
        }
        return getResponse400(null);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getById(UUID id) {
        User entity = userRepository.findById(id).orElse(null);
        if (entity == null || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.USER)));
        }
        return getResponse200(new UserDetailDto(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getPage(UserSearchRequest request) {
        return getResponse200(userRepository.getPages(entityManager, request, getPageable(request)));
    }

    @Override
    @Transactional
    public BaseResponse create(UserCreateRequest request) {
        HashMap<String, String> errors = validation(request);
        if (userRepository.existsByUsername(request.getUsername())) {
            errors.put(SystemVariable.USERNAME, getMessage(SystemMessage.VALUE_EXIST, request.getUsername()));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            errors.put(SystemVariable.EMAIL, getMessage(SystemMessage.VALUE_EXIST, request.getEmail()));
        }
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }

        Set<Role> roles = resolveRoles(request.getRoleCodes());
        User entity = new User();
        entity.setUsername(request.getUsername());
        entity.setPassword(passwordEncoder.encode(request.getPassword()));
        entity.setEmail(request.getEmail());
        entity.setFullName(request.getFullName());
        entity.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        entity.setAccountNonExpired(true);
        entity.setAccountNonLocked(true);
        entity.setCredentialsNonExpired(true);
        entity.setVoided(false);
        entity.setRoles(roles);

        entity = userRepository.save(entity);
        return getResponse201(new UserDetailDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional
    public BaseResponse update(UUID id, UserUpdateRequest request) {
        HashMap<String, String> errors = validation(request);
        User entity = userRepository.findById(id).orElse(null);
        if (entity == null || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.USER)));
        }
        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
            errors.put(SystemVariable.USERNAME, getMessage(SystemMessage.VALUE_EXIST, request.getUsername()));
        }
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            errors.put(SystemVariable.EMAIL, getMessage(SystemMessage.VALUE_EXIST, request.getEmail()));
        }
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }

        BeanUtils.copyProperties(request, entity, ConstUtil.NON_UPDATABLE_FIELDS);
        if (request.getRoleCodes() != null) {
            entity.setRoles(resolveRoles(request.getRoleCodes()));
        }
        entity = userRepository.save(entity);
        return getResponse200(new UserDetailDto(entity));
    }

    @Override
    @Transactional
    public BaseResponse delete(UUID id) {
        User entity = userRepository.findById(id).orElse(null);
        if (entity == null || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.USER)));
        }
        entity.setVoided(true);
        entity = userRepository.save(entity);
        return getResponse200(new UserDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional
    public BaseResponse updatePassword(UUID id, UserPasswordUpdateRequest request) {
        HashMap<String, String> errors = validation(request);
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        User entity = userRepository.findById(id).orElse(null);
        if (entity == null || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.USER)));
        }
        entity.setPassword(passwordEncoder.encode(request.getNewPassword()));
        entity = userRepository.save(entity);
        return getResponse200(true, getMessage(SystemMessage.SUCCESS));
    }

    private Set<Role> resolveRoles(Set<String> roleCodes) {
        if (CollectionUtils.isEmpty(roleCodes)) {
            Role userRole = roleRepository.findByCode(RoleEnum.USER.getCode())
                    .orElseGet(() -> roleRepository.findByName(ConstUtil.USER_ROLE)
                            .orElse(Role.builder().code(RoleEnum.USER.getCode()).name(ConstUtil.USER_ROLE).build()));
            return Set.of(userRole);
        }
        return roleCodes.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .map(code -> roleRepository.findByCode(code).orElseGet(() -> Role.builder().code(code).name(code).build()))
                .collect(Collectors.toSet());
    }
}
