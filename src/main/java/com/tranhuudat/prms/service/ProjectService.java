package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.request.ProjectRequest;
import com.tranhuudat.prms.dto.request.search.ProjectSearchRequest;
import com.tranhuudat.prms.dto.response.PageResponse;
import com.tranhuudat.prms.dto.response.ProjectResponse;
import com.tranhuudat.prms.entity.Project;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.exception.AppException;
import com.tranhuudat.prms.exception.ErrorCode;
import com.tranhuudat.prms.repository.ProjectRepository;
import com.tranhuudat.prms.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectService extends BaseService<Project, UUID> {
    ProjectRepository projectRepository;
    UserRepository userRepository;

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        }

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .code(request.getCode())
                .manager(manager)
                .priority(request.getPriority())
                .status(request.getStatus())
                .progressPercentage(request.getProgressPercentage())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setShortDescription(request.getShortDescription());
        project.setCode(request.getCode());
        project.setManager(manager);
        project.setPriority(request.getPriority());
        project.setStatus(request.getStatus());
        project.setProgressPercentage(request.getProgressPercentage());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    public ProjectResponse getById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        return mapToResponse(project);
    }

    @Transactional
    public void delete(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        project.setVoided(true);
        projectRepository.save(project);
    }

    public PageResponse<ProjectResponse> getAll(ProjectSearchRequest request) {
        Pageable pageable = getPageable(
                request.getPageIndex(), 
                request.getPageSize(), 
                request.getSortBy(), 
                request.getDirection());
        
        Page<Project> pageData = projectRepository.findAll(pageable);
        
        return toPageResponse(pageData, pageData.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .shortDescription(project.getShortDescription())
                .code(project.getCode())
                .managerId(project.getManager() != null ? project.getManager().getId() : null)
                .managerFullName(project.getManager() != null ? project.getManager().getFullName() : null)
                .priority(project.getPriority())
                .status(project.getStatus())
                .progressPercentage(project.getProgressPercentage())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .build();
    }
}
