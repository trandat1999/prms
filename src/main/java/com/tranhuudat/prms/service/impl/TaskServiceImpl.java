package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.task.TaskAssignRequest;
import com.tranhuudat.prms.dto.task.TaskKanbanBoardDto;
import com.tranhuudat.prms.dto.task.TaskKanbanBoardUpdateRequest;
import com.tranhuudat.prms.dto.task.TaskKanbanColumnDto;
import com.tranhuudat.prms.dto.task.TaskDto;
import com.tranhuudat.prms.dto.task.TaskLogDto;
import com.tranhuudat.prms.dto.task.TaskSearchRequest;
import com.tranhuudat.prms.dto.task.TaskStatusUpdateRequest;
import com.tranhuudat.prms.entity.Task;
import com.tranhuudat.prms.entity.TaskLog;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.enums.TaskLogActionEnum;
import com.tranhuudat.prms.enums.TaskStatusEnum;
import com.tranhuudat.prms.repository.TaskLogRepository;
import com.tranhuudat.prms.repository.TaskRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.TaskService;
import com.tranhuudat.prms.util.ConstUtil;
import com.tranhuudat.prms.util.SystemMessage;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends BaseService implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public BaseResponse create(TaskDto request) {
        HashMap<String, String> errors = validation(request);
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        Task entity = new Task();
        BeanUtils.copyProperties(request, entity, ConstUtil.NON_UPDATABLE_FIELDS);
        entity.setVoided(false);
        entity = taskRepository.save(entity);

        saveLog(entity.getId(), TaskLogActionEnum.CREATE, null, snapshot(entity));
        return getResponse201(new TaskDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional
    public BaseResponse update(UUID id, TaskDto request) {
        HashMap<String, String> errors = validation(request);
        Task entity = taskRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, "task"));
        }
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        String oldSnapshot = snapshot(entity);
        // Không xử lý assign/status ở đây để log đúng action chuyên biệt
        UUID keepAssigned = entity.getAssignedId();
        var keepStatus = entity.getStatus();
        BeanUtils.copyProperties(request, entity, ConstUtil.NON_UPDATABLE_FIELDS);
        entity.setAssignedId(keepAssigned);
        entity.setStatus(keepStatus);
        entity = taskRepository.save(entity);
        saveLog(entity.getId(), TaskLogActionEnum.UPDATE, oldSnapshot, snapshot(entity));
        return getResponse200(new TaskDto(entity));
    }

    @Override
    @Transactional
    public BaseResponse delete(UUID id) {
        Task entity = taskRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, "task"));
        }
        entity.setVoided(true);
        entity = taskRepository.save(entity);
        return getResponse200(new TaskDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getById(UUID id) {
        Task entity = taskRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, "task"));
        }
        return getResponse200(new TaskDto(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getPage(TaskSearchRequest request) {
        return getResponse200(taskRepository.getPages(entityManager, request, getPageable(request)));
    }

    @Override
    @Transactional
    public BaseResponse assign(UUID id, TaskAssignRequest request) {
        HashMap<String, String> errors = validation(request);
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        Task entity = taskRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, "task"));
        }
        UUID oldAssignedId = entity.getAssignedId();
        UUID newAssignedId = request.getAssignedId();
        if (Objects.equals(oldAssignedId, newAssignedId)) {
            return getResponse200(true, getMessage(SystemMessage.SUCCESS));
        }
        User newUser = userRepository.findById(newAssignedId).orElse(null);
        if (newUser == null || Boolean.TRUE.equals(newUser.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, "user"));
        }
        String newDisplay = userDisplay(newUser);
        TaskLogActionEnum action = oldAssignedId == null ? TaskLogActionEnum.ASSIGN : TaskLogActionEnum.REASSIGN;
        String oldDisplay = null;
        if (oldAssignedId != null) {
            oldDisplay = userRepository.findById(oldAssignedId).map(this::userDisplay).orElse(null);
        }

        entity.setAssignedId(newAssignedId);
        entity = taskRepository.save(entity);
        saveLog(entity.getId(), action, oldDisplay, newDisplay);
        return getResponse200(new TaskDto(entity));
    }

    @Override
    @Transactional
    public BaseResponse updateStatus(UUID id, TaskStatusUpdateRequest request) {
        HashMap<String, String> errors = validation(request);
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        Task entity = taskRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, "task"));
        }
        var oldStatus = entity.getStatus();
        var newStatus = request.getStatus();
        if (Objects.equals(oldStatus, newStatus)) {
            return getResponse200(new TaskDto(entity));
        }
        entity.setStatus(newStatus);
        entity = taskRepository.save(entity);
        saveLog(entity.getId(), TaskLogActionEnum.STATUS_CHANGE,
                oldStatus != null ? oldStatus.name() : null,
                newStatus != null ? newStatus.name() : null);
        return getResponse200(new TaskDto(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getLogs(UUID id) {
        List<TaskLogDto> logs = taskLogRepository.findAllByTaskIdOrderByCreatedDateDesc(id).stream()
                .filter(l -> l.getVoided() == null || !l.getVoided())
                .map(TaskLogDto::new)
                .toList();
        return getResponse200(logs);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getKanbanBoard(UUID projectId) {
        if (projectId == null) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST));
        }
        List<TaskDto> rows = taskRepository.getKanbanTasks(projectId);
        Map<TaskStatusEnum, List<TaskDto>> byStatus = rows.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus() != null ? t.getStatus() : TaskStatusEnum.TODO));

        List<TaskKanbanColumnDto> columns = List.of(
                new TaskKanbanColumnDto(TaskStatusEnum.TODO, TaskStatusEnum.TODO.getName(), byStatus.getOrDefault(TaskStatusEnum.TODO, List.of())),
                new TaskKanbanColumnDto(TaskStatusEnum.IN_PROGRESS, TaskStatusEnum.IN_PROGRESS.getName(), byStatus.getOrDefault(TaskStatusEnum.IN_PROGRESS, List.of())),
                new TaskKanbanColumnDto(TaskStatusEnum.REVIEW, TaskStatusEnum.REVIEW.getName(), byStatus.getOrDefault(TaskStatusEnum.REVIEW, List.of())),
                new TaskKanbanColumnDto(TaskStatusEnum.TESTING, TaskStatusEnum.TESTING.getName(), byStatus.getOrDefault(TaskStatusEnum.TESTING, List.of())),
                new TaskKanbanColumnDto(TaskStatusEnum.DONE, TaskStatusEnum.DONE.getName(), byStatus.getOrDefault(TaskStatusEnum.DONE, List.of()))
        );
        return getResponse200(new TaskKanbanBoardDto(projectId, columns));
    }

    @Override
    @Transactional
    public BaseResponse updateKanbanBoard(TaskKanbanBoardUpdateRequest request) {
        HashMap<String, String> errors = validation(request);
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        UUID projectId = request.getProjectId();
        var columns = request.getColumns();
        if (columns == null) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST));
        }

        // Map id -> target status/order
        Map<UUID, TaskStatusEnum> targetStatus = new HashMap<>();
        Map<UUID, Integer> targetOrder = new HashMap<>();
        for (var col : columns) {
            if (col == null || col.getStatus() == null || col.getTaskIds() == null) continue;
            int idx = 0;
            for (UUID id : col.getTaskIds()) {
                if (id == null) continue;
                targetStatus.put(id, col.getStatus());
                targetOrder.put(id, idx++);
            }
        }
        if (targetStatus.isEmpty()) {
            return getResponse200(true, getMessage(SystemMessage.SUCCESS));
        }

        List<UUID> ids = targetStatus.keySet().stream().toList();
        List<Task> tasks = taskRepository.findAllById(ids).stream()
                .filter(t -> t != null && (t.getVoided() == null || !t.getVoided()))
                .filter(t -> Objects.equals(t.getProjectId(), projectId))
                .toList();

        // Update tasks (status + order). Log status changes.
        List<Task> toSave = tasks.stream().map(t -> {
            TaskStatusEnum oldStatus = t.getStatus();
            TaskStatusEnum newStatus = targetStatus.get(t.getId());
            Integer newOrder = targetOrder.get(t.getId());
            if (newStatus != null) {
                t.setStatus(newStatus);
            }
            t.setKanbanOrder(newOrder);
            if (newStatus != null && !Objects.equals(oldStatus, newStatus)) {
                saveLog(t.getId(), TaskLogActionEnum.STATUS_CHANGE,
                        oldStatus != null ? oldStatus.name() : null,
                        newStatus.name());
            }
            return t;
        }).toList();

        taskRepository.saveAll(toSave);
        return getResponse200(true, getMessage(SystemMessage.SUCCESS));
    }

    private void saveLog(UUID taskId, TaskLogActionEnum action, String oldValue, String newValue) {
        TaskLog log = TaskLog.builder()
                .taskId(taskId)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .voided(false)
                .build();
        taskLogRepository.save(log);
    }

    private String userDisplay(User u) {
        String username = u.getUsername();
        String fullName = u.getFullName();
        String left = username != null ? username : "";
        String right = fullName != null ? fullName : "";
        if (left.isBlank() && right.isBlank()) return null;
        return left + " - " + right;
    }

    private String snapshot(Task t) {
        if (t == null) return null;
        return "code=" + nullSafe(t.getCode()) +
                ";name=" + nullSafe(t.getName()) +
                ";projectId=" + (t.getProjectId() != null ? t.getProjectId() : "") +
                ";status=" + (t.getStatus() != null ? t.getStatus().name() : "") +
                ";priority=" + (t.getPriority() != null ? t.getPriority().name() : "") +
                ";type=" + nullSafe(t.getType()) +
                ";estimatedHours=" + (t.getEstimatedHours() != null ? t.getEstimatedHours() : "") +
                ";actualHours=" + (t.getActualHours() != null ? t.getActualHours() : "") +
                ";assignedId=" + (t.getAssignedId() != null ? t.getAssignedId() : "") +
                ";label=" + nullSafe(t.getLabel());
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}

