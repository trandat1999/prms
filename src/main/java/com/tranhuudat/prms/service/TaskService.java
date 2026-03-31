package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.request.TaskRequest;
import com.tranhuudat.prms.dto.request.search.TaskSearchRequest;
import com.tranhuudat.prms.dto.response.PageResponse;
import com.tranhuudat.prms.dto.response.TaskResponse;
import com.tranhuudat.prms.entity.Project;
import com.tranhuudat.prms.entity.Task;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.exception.AppException;
import com.tranhuudat.prms.exception.ErrorCode;
import com.tranhuudat.prms.repository.ProjectRepository;
import com.tranhuudat.prms.repository.TaskRepository;
import com.tranhuudat.prms.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskService extends BaseService<Task, UUID> {
    TaskRepository taskRepository;
    ProjectRepository projectRepository;
    UserRepository userRepository;

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        Set<User> assignedUsers = new HashSet<>();
        if (request.getAssignedUserIds() != null) {
            assignedUsers = request.getAssignedUserIds().stream()
                    .map(id -> userRepository.findById(id)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)))
                    .collect(Collectors.toSet());
        }

        Task task = Task.builder()
                .name(request.getName())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .code(request.getCode())
                .project(project)
                .status(request.getStatus())
                .priority(request.getPriority())
                .estimatedHours(request.getEstimatedHours())
                .actualHours(request.getActualHours())
                .dueDate(request.getDueDate())
                .assignedUsers(assignedUsers)
                .build();

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse update(UUID id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        Set<User> assignedUsers = new HashSet<>();
        if (request.getAssignedUserIds() != null) {
            assignedUsers = request.getAssignedUserIds().stream()
                    .map(userId -> userRepository.findById(userId)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)))
                    .collect(Collectors.toSet());
        }

        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setShortDescription(request.getShortDescription());
        task.setCode(request.getCode());
        task.setProject(project);
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setActualHours(request.getActualHours());
        task.setDueDate(request.getDueDate());
        task.setAssignedUsers(assignedUsers);

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    public TaskResponse getById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        return mapToResponse(task);
    }

    @Transactional
    public void delete(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        task.setVoided(true);
        taskRepository.save(task);
    }

    public PageResponse<TaskResponse> getAll(TaskSearchRequest request) {
        Pageable pageable = getPageable(
                request.getPageIndex(),
                request.getPageSize(),
                request.getSortBy(),
                request.getDirection());

        Page<Task> pageData = taskRepository.findAll(pageable);

        return toPageResponse(pageData, pageData.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .shortDescription(task.getShortDescription())
                .code(task.getCode())
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .projectName(task.getProject() != null ? task.getProject().getName() : null)
                .status(task.getStatus())
                .priority(task.getPriority())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .dueDate(task.getDueDate())
                .assignedUserIds(task.getAssignedUsers().stream()
                        .map(User::getId)
                        .collect(Collectors.toSet()))
                .assignedUserNames(task.getAssignedUsers().stream()
                        .map(User::getFullName)
                        .collect(Collectors.toSet()))
                .build();
    }
}
