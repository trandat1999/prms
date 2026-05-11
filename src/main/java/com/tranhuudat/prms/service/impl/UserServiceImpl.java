package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.user.CurrentUserPasswordUpdateRequest;
import com.tranhuudat.prms.dto.user.CurrentUserProfileUpdateRequest;
import com.tranhuudat.prms.dto.user.UserDto;
import com.tranhuudat.prms.dto.user.UserCreateRequest;
import com.tranhuudat.prms.dto.user.UserDetailDto;
import com.tranhuudat.prms.dto.user.UserPasswordUpdateRequest;
import com.tranhuudat.prms.dto.user.UserSearchRequest;
import com.tranhuudat.prms.dto.user.UserUpdateRequest;
import com.tranhuudat.prms.dto.user.UserSkillsUpdateRequest;
import com.tranhuudat.prms.dto.user.UserSkillWriteDto;
import com.tranhuudat.prms.dto.user.UserSkillDto;
import com.tranhuudat.prms.entity.Role;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.entity.Skill;
import com.tranhuudat.prms.entity.UserSkill;
import com.tranhuudat.prms.enums.RoleEnum;
import com.tranhuudat.prms.repository.RoleRepository;
import com.tranhuudat.prms.repository.SkillRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.repository.UserSkillRepository;
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
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.Objects;
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
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
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

    @Override
    @Transactional
    public BaseResponse updateCurrentUserProfile(CurrentUserProfileUpdateRequest request) {
        HashMap<String, String> errors = validation(request);
        Optional<User> selfOpt = getAuthenticatedUser();
        if (selfOpt.isEmpty()) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST));
        }
        User entity = selfOpt.get();
        if (Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.USER)));
        }
        UUID id = entity.getId();
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            errors.put(SystemVariable.EMAIL, getMessage(SystemMessage.VALUE_EXIST, request.getEmail()));
        }
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        entity.setEmail(request.getEmail());
        entity.setFullName(request.getFullName());
        entity = userRepository.save(entity);
        return getResponse200(new UserDto(entity));
    }

    @Override
    @Transactional
    public BaseResponse updateCurrentUserPassword(CurrentUserPasswordUpdateRequest request) {
        HashMap<String, String> errors = validation(request);
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        Optional<User> selfOpt = getAuthenticatedUser();
        if (selfOpt.isEmpty()) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST));
        }
        User entity = selfOpt.get();
        if (Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.USER)));
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), entity.getPassword())) {
            HashMap<String, String> pwdErrors = new HashMap<>();
            pwdErrors.put(
                    SystemVariable.CURRENT_PASSWORD,
                    getMessage(SystemMessage.INVALID_CURRENT_PASSWORD));
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), pwdErrors);
        }
        entity.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(entity);
        return getResponse200(true, getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getSkills(UUID id) {
        User entity = userRepository.findById(id).orElse(null);
        if (entity == null || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.USER)));
        }
        var rows = userSkillRepository.findByUserIdAndVoidedFalseOrderByCreatedDateDesc(id);
        var list = rows.stream().map(UserSkillDto::new).toList();
        return getResponse200(list);
    }

    @Override
    @Transactional
    public BaseResponse updateSkills(UUID id, UserSkillsUpdateRequest request) {
        HashMap<String, String> errors = validation(request);
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        User entity = userRepository.findById(id).orElse(null);
        if (entity == null || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.USER)));
        }
        List<UserSkillWriteDto> items = request.getItems();
        if (CollectionUtils.isEmpty(items)) {
            userSkillRepository.deleteByUserId(id);
            return getResponse200(List.of(), getMessage(SystemMessage.SUCCESS));
        }

        // validate duplicate skillId
        boolean dup = items.stream()
                .map(UserSkillWriteDto::getSkillId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()))
                .entrySet().stream()
                .anyMatch(e -> e.getValue() != null && e.getValue() > 1);
        if (dup) {
            HashMap<String, String> dupErrors = new HashMap<>();
            dupErrors.put(SystemVariable.SKILL_IDS, getMessage(SystemMessage.BAD_REQUEST));
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), dupErrors);
        }

        List<UUID> skillIds = items.stream()
                .map(UserSkillWriteDto::getSkillId)
                .filter(Objects::nonNull)
                .toList();
        List<Skill> skills = skillRepository.findAllById(skillIds);
        if (skills.size() != skillIds.size()) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.SKILL)));
        }
        var skillMap = skills.stream().collect(Collectors.toMap(Skill::getId, s -> s));

        // update-in-place để tránh lỗi unique (Hibernate có thể insert trước delete trong 1 transaction)
        List<UserSkill> existing = userSkillRepository.findByUserId(id);
        var existingMap = existing.stream()
                .filter(e -> Objects.nonNull(e.getSkillId()))
                .collect(Collectors.toMap(UserSkill::getSkillId, e -> e, (a, b) -> a));

        var incomingSkillIds = skillIds.stream().collect(Collectors.toSet());
        var toDelete = existing.stream()
                .filter(e -> Objects.nonNull(e.getSkillId()) && !incomingSkillIds.contains(e.getSkillId()))
                .toList();
        if (!CollectionUtils.isEmpty(toDelete)) {
            userSkillRepository.deleteAllInBatch(toDelete);
        }

        List<UserSkill> toSave = items.stream().map(it -> {
            UUID skillId = it.getSkillId();
            UserSkill us = existingMap.get(skillId);
            if (us == null) {
                us = new UserSkill();
                us.setUserId(id);
                us.setSkillId(skillId);
            }
            us.setVoided(false);
            us.setLevel(it.getLevel());
            us.setExperienceYear(it.getExperienceYear());
            us.setLastUsedDate(it.getLastUsedDate());
            us.setIsPrimary(Boolean.TRUE.equals(it.getIsPrimary()));
            us.setSkill(skillMap.get(skillId));
            us.setUser(entity);
            return us;
        }).toList();
        List<UserSkill> saved = userSkillRepository.saveAll(toSave);

        List<UserSkillDto> list = saved.stream().map(UserSkillDto::new).toList();
        return getResponse200(list, getMessage(SystemMessage.SUCCESS));
    }

    private Optional<User> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return Optional.empty();
        }
        return userRepository.findByUsername(authentication.getName());
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
