package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.request.ProjectRequest;
import com.tranhuudat.prms.dto.request.search.ProjectSearchRequest;
import com.tranhuudat.prms.dto.response.PageResponse;
import com.tranhuudat.prms.dto.response.ProjectResponse;
import com.tranhuudat.prms.entity.Project;
import com.tranhuudat.prms.exception.AppException;
import com.tranhuudat.prms.exception.ErrorCode;
import com.tranhuudat.prms.repository.ProjectRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.ProjectService;
import com.tranhuudat.prms.util.SystemMessage;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl extends BaseService implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BaseResponse create(ProjectRequest request) {
        HashMap<String, String> errors = validation(request);
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        if (projectRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.PROJECT_CODE_EXISTS);
        }
        if (request.getManagerId() != null && !userRepository.existsById(request.getManagerId())) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        Project project = mapRequestToEntity(request, new Project());
        projectRepository.save(project);
        return getResponse201(mapToResponse(project), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional
    public BaseResponse update(UUID id, ProjectRequest request) {
        HashMap<String, String> errors = validation(request);
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        Project project = getActiveProject(id);
        if (projectRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new AppException(ErrorCode.PROJECT_CODE_EXISTS);
        }
        if (request.getManagerId() != null && !userRepository.existsById(request.getManagerId())) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        mapRequestToEntity(request, project);
        projectRepository.save(project);
        return getResponse200(mapToResponse(project));
    }

    @Override
    @Transactional
    public BaseResponse delete(UUID id) {
        Project project = getActiveProject(id);
        project.setVoided(true);
        projectRepository.save(project);
        return getResponse200(null, getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getById(UUID id) {
        Project project = getActiveProject(id);
        return getResponse200(mapToResponse(project));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getPage(ProjectSearchRequest request) {
        Pageable base = getPageable(request);
        Pageable pageable = PageRequest.of(
                base.getPageNumber(),
                base.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdDate")
        );
        Specification<Project> spec = buildSpecification(request);
        Page<Project> page = projectRepository.findAll(spec, pageable);
        List<ProjectResponse> content = page.getContent().stream().map(this::mapToResponse).toList();
        PageResponse pageResponse = toPageResponse(page, content);
        return getResponse200(pageResponse);
    }

    private Project getActiveProject(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        if (Boolean.TRUE.equals(project.getVoided())) {
            throw new AppException(ErrorCode.PROJECT_NOT_FOUND);
        }
        return project;
    }

    private Specification<Project> buildSpecification(ProjectSearchRequest req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (Boolean.TRUE.equals(req.getVoided())) {
                predicates.add(cb.isTrue(root.get("voided")));
            } else {
                predicates.add(cb.or(
                        cb.isFalse(root.get("voided")),
                        cb.isNull(root.get("voided"))
                ));
            }

            if (req.getManagerId() != null) {
                predicates.add(cb.equal(root.get("manager").get("id"), req.getManagerId()));
            }
            if (req.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), req.getStatus()));
            }
            if (req.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), req.getStartDate()));
            }
            if (req.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), req.getEndDate()));
            }
            if (StringUtils.hasText(req.getKeyword())) {
                String kw = "%" + req.getKeyword().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), kw),
                        cb.like(cb.lower(root.get("code")), kw),
                        cb.like(cb.lower(root.get("description")), kw)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Project mapRequestToEntity(ProjectRequest request, Project project) {
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setShortDescription(request.getShortDescription());
        project.setCode(request.getCode());
        project.setPriority(request.getPriority());
        project.setStatus(request.getStatus());
        project.setProgressPercentage(request.getProgressPercentage());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        if (request.getManagerId() != null) {
            project.setManager(userRepository.getReferenceById(request.getManagerId()));
        } else {
            project.setManager(null);
        }
        return project;
    }

    private ProjectResponse mapToResponse(Project project) {
        UUID managerId = project.getManager() != null ? project.getManager().getId() : null;
        String managerFullName = project.getManager() != null ? project.getManager().getFullName() : null;
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .shortDescription(project.getShortDescription())
                .code(project.getCode())
                .managerId(managerId)
                .managerFullName(managerFullName)
                .priority(project.getPriority())
                .status(project.getStatus())
                .progressPercentage(project.getProgressPercentage())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .build();
    }
}
