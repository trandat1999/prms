package com.tranhuudat.prms.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.notification.UserNotificationDto;
import com.tranhuudat.prms.dto.task.TaskAssignRequest;
import com.tranhuudat.prms.dto.task.TaskChecklistDto;
import com.tranhuudat.prms.dto.task.TaskChecklistToggleRequest;
import com.tranhuudat.prms.dto.task.TaskKanbanBoardDto;
import com.tranhuudat.prms.dto.task.TaskKanbanBoardUpdateRequest;
import com.tranhuudat.prms.dto.task.TaskKanbanColumnDto;
import com.tranhuudat.prms.dto.task.TaskDto;
import com.tranhuudat.prms.dto.task.TaskLogDto;
import com.tranhuudat.prms.dto.task.TaskRefDto;
import com.tranhuudat.prms.dto.task.TaskSearchRequest;
import com.tranhuudat.prms.dto.task.TaskStatusUpdateRequest;
import com.tranhuudat.prms.entity.Project;
import com.tranhuudat.prms.entity.Task;
import com.tranhuudat.prms.entity.TaskChecklist;
import com.tranhuudat.prms.entity.TaskDependency;
import com.tranhuudat.prms.entity.TaskLog;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.enums.TaskLogActionEnum;
import com.tranhuudat.prms.enums.TaskStatusEnum;
import com.tranhuudat.prms.enums.ProjectStatusEnum;
import com.tranhuudat.prms.enums.NotificationChannelEnum;
import com.tranhuudat.prms.enums.NotificationDeliveryStatusEnum;
import com.tranhuudat.prms.enums.NotificationTypeEnum;
import com.tranhuudat.prms.entity.Notification;
import com.tranhuudat.prms.entity.NotificationDelivery;
import com.tranhuudat.prms.repository.ProjectMemberRepository;
import com.tranhuudat.prms.repository.ProjectRepository;
import com.tranhuudat.prms.repository.NotificationRepository;
import com.tranhuudat.prms.repository.NotificationDeliveryRepository;
import com.tranhuudat.prms.repository.TaskChecklistRepository;
import com.tranhuudat.prms.repository.TaskDependencyRepository;
import com.tranhuudat.prms.repository.TaskLogRepository;
import com.tranhuudat.prms.repository.TaskRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.TaskService;
import com.tranhuudat.prms.service.UserNotificationStreamService;
import com.tranhuudat.prms.service.UserWebPushService;
import com.tranhuudat.prms.config.AppProperties;
import com.tranhuudat.prms.util.ConstUtil;
import com.tranhuudat.prms.util.SystemMessage;
import com.tranhuudat.prms.util.SystemVariable;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends BaseService implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final EntityManager entityManager;
    private final TaskChecklistRepository taskChecklistRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final UserNotificationStreamService userNotificationStreamService;
    private final UserWebPushService userWebPushService;
    private final AppProperties appProperties;

    private static String[] taskDtoRelationIgnore() {
        String[] n = ConstUtil.NON_UPDATABLE_FIELDS;
        String[] extra = {
                "checklists",
                "predecessorTaskIds",
                "predecessors",
                "projectName",
                "assignedDisplay",
                "reporterDisplay",
                "reviewerDisplay",
                "parentTaskCode"
        };
        String[] r = new String[n.length + extra.length];
        System.arraycopy(n, 0, r, 0, n.length);
        System.arraycopy(extra, 0, r, n.length, extra.length);
        return r;
    }

    @Override
    @Transactional
    public BaseResponse create(TaskDto request) {
        HashMap<String, String> errors = validation(request);
        errors.putAll(validateChecklists(request.getChecklists()));
        // PM-only create task (trong phạm vi dự án)
        
        if (Objects.nonNull(request.getProjectId())) {
            UUID currentUserId = resolveCurrentUserId();
            if (Objects.nonNull(request.getProjectId())) {
                Project project = projectRepository.findById(request.getProjectId()).orElse(null);
                if (Objects.nonNull(project) && !Boolean.TRUE.equals(project.getVoided())) {
                    boolean isPm = Objects.equals(project.getManagerId(), currentUserId);
                    if (!isPm) {
            errors.put(SystemVariable.PROJECT_ID, getMessage(SystemMessage.FORBIDDEN));
                    }
                }
            }
        }
        UUID reporterForCheck =
                Objects.nonNull(request.getReporterId()) ? request.getReporterId() : resolveCurrentUserId();
        errors.putAll(validateTaskProjectAndMembers(
                request.getProjectId(),
                request.getAssignedId(),
                reporterForCheck,
                request.getReviewerId(),
                request.getParentTaskId(),
                null));
        List<UUID> preds = normalizePredecessorIds(request.getPredecessorTaskIds());
        errors.putAll(validatePredecessorTasks(request.getProjectId(), null, preds));
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        Task entity = new Task();
        BeanUtils.copyProperties(request, entity, taskDtoRelationIgnore());
        entity.setVoided(false);
        if (Objects.isNull(entity.getReporterId())) {
            entity.setReporterId(resolveCurrentUserId());
        }
        applyChecklistEstimatedHoursIfAny(entity, request.getChecklists());
        entity = taskRepository.save(entity);
        replaceChecklists(entity.getId(), Optional.ofNullable(request.getChecklists()).orElseGet(List::of));
        replacePredecessors(entity.getId(), entity.getProjectId(), preds);
        saveLog(entity.getId(), TaskLogActionEnum.CREATE, null, snapshot(entity));
        notifyTaskAssigned(entity, entity.getAssignedId(), resolveCurrentUserId());
        return getResponse201(buildTaskDtoWithRelations(entity.getId()), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional
    public BaseResponse update(UUID id, TaskDto request) {
        HashMap<String, String> errors = validation(request);
        if (Objects.nonNull(request.getChecklists())) {
            errors.putAll(validateChecklists(request.getChecklists()));
        }
        Task entity = taskRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.TASK)));
        }
        UUID effectiveProjectId =
                Objects.nonNull(request.getProjectId()) ? request.getProjectId() : entity.getProjectId();
        UUID reporterForCheck =
                Objects.nonNull(request.getReporterId()) ? request.getReporterId() : entity.getReporterId();
        UUID reviewerForCheck =
                Objects.nonNull(request.getReviewerId()) ? request.getReviewerId() : entity.getReviewerId();
        errors.putAll(validateTaskProjectAndMembers(
                effectiveProjectId,
                entity.getAssignedId(),
                reporterForCheck,
                reviewerForCheck,
                request.getParentTaskId(),
                id));
        if (Objects.nonNull(request.getPredecessorTaskIds())) {
            List<UUID> predNorm = normalizePredecessorIds(request.getPredecessorTaskIds());
            errors.putAll(validatePredecessorTasks(effectiveProjectId, id, predNorm));
            errors.putAll(validateDependencyGraph(effectiveProjectId, id, predNorm));
        }
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        String oldSnapshot = snapshot(entity);
        UUID keepAssigned = entity.getAssignedId();
        var keepStatus = entity.getStatus();
        BeanUtils.copyProperties(request, entity, taskDtoRelationIgnore());
        entity.setAssignedId(keepAssigned);
        entity.setStatus(keepStatus);
        if (Objects.nonNull(request.getChecklists())) {
            applyChecklistEstimatedHoursIfAny(entity, request.getChecklists());
        }
        entity = taskRepository.save(entity);
        if (Objects.nonNull(request.getChecklists())) {
            replaceChecklists(entity.getId(), request.getChecklists());
        }
        if (Objects.nonNull(request.getPredecessorTaskIds())) {
            replacePredecessors(entity.getId(), effectiveProjectId, normalizePredecessorIds(request.getPredecessorTaskIds()));
        }
        saveLog(entity.getId(), TaskLogActionEnum.UPDATE, oldSnapshot, snapshot(entity));
        return getResponse200(buildTaskDtoWithRelations(entity.getId()));
    }

    @Override
    @Transactional
    public BaseResponse delete(UUID id) {
        Task entity = taskRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.TASK)));
        }
        voidTaskRelations(id);
        entity.setVoided(true);
        entity = taskRepository.save(entity);
        return getResponse200(new TaskDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getById(UUID id) {
        Task entity = taskRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.TASK)));
        }
        return getResponse200(buildTaskDtoWithRelations(id));
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
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.TASK)));
        }
        UUID oldAssignedId = entity.getAssignedId();
        UUID newAssignedId = request.getAssignedId();
        if (Objects.equals(oldAssignedId, newAssignedId)) {
            return getResponse200(true, getMessage(SystemMessage.SUCCESS));
        }
        User newUser = userRepository.findById(newAssignedId).orElse(null);
        if (newUser == null || Boolean.TRUE.equals(newUser.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.USER)));
        }
        if (Objects.nonNull(entity.getProjectId())
                && !projectMemberRepository.existsActiveMember(entity.getProjectId(), newAssignedId)) {
            return getResponse400(
                    getMessage(SystemMessage.BAD_REQUEST),
                    Map.of(SystemVariable.ASSIGNED_ID, getMessage(SystemMessage.NOT_PROJECT_MEMBER)));
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
        notifyTaskAssigned(entity, newAssignedId, resolveCurrentUserId());
        return getResponse200(buildTaskDtoWithRelations(entity.getId()));
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
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.TASK)));
        }
        var oldStatus = entity.getStatus();
        var newStatus = request.getStatus();
        if (Objects.equals(oldStatus, newStatus)) {
            return getResponse200(buildTaskDtoWithRelations(id));
        }
        if (isBlockedByPredecessors(entity.getId(), newStatus)) {
            return getResponse400(
                    getMessage(SystemMessage.BAD_REQUEST),
                    Map.of(SystemVariable.STATUS, getMessage(SystemMessage.TASK_PREDECESSOR_NOT_DONE)));
        }
        entity.setStatus(newStatus);
        entity = taskRepository.save(entity);
        saveLog(entity.getId(), TaskLogActionEnum.STATUS_CHANGE,
                oldStatus != null ? oldStatus.name() : null,
                newStatus != null ? newStatus.name() : null);
        if (!Objects.equals(oldStatus, TaskStatusEnum.DONE) && Objects.equals(newStatus, TaskStatusEnum.DONE)) {
            completeAllChecklists(entity.getId());
            notifySuccessors(entity);
        }
        return getResponse200(buildTaskDtoWithRelations(id));
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
    public BaseResponse getChecklists(UUID taskId) {
        if (taskId == null) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST));
        }
        Task t = taskRepository.findById(taskId).orElse(null);
        if (t == null || Boolean.TRUE.equals(t.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.TASK)));
        }
        List<TaskChecklistDto> rows = taskChecklistRepository
                .findByTaskIdAndVoidedFalseOrderBySortOrderAscCreatedDateAsc(taskId)
                .stream()
                .map(TaskChecklistDto::new)
                .toList();
        return getResponse200(rows);
    }

    @Override
    @Transactional
    public BaseResponse toggleChecklist(UUID taskId, UUID checklistId, TaskChecklistToggleRequest request) {
        HashMap<String, String> errors = validation(request);
        if (taskId == null || checklistId == null) {
            errors.put(SystemVariable.ID, getMessage(SystemMessage.BAD_REQUEST));
        }
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        Task t = taskRepository.findById(taskId).orElse(null);
        if (t == null || Boolean.TRUE.equals(t.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.TASK)));
        }
        TaskChecklist row = taskChecklistRepository.findById(checklistId).orElse(null);
        if (row == null || Boolean.TRUE.equals(row.getVoided()) || !Objects.equals(row.getTaskId(), taskId)) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.CHECKLIST)));
        }
        row.setChecked(Boolean.TRUE.equals(request.getChecked()));
        taskChecklistRepository.save(row);
        // trả về list để UI sync nhanh
        return getChecklists(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getKanbanBoard(UUID projectId) {
        List<TaskDto> rows;
        if (projectId == null) {
            UUID uid = resolveCurrentUserId();
            List<UUID> visibleProjectIds =
                    projectRepository.findVisibleProjectIdsForUser(entityManager, uid, ProjectStatusEnum.IN_PROGRESS);
            if (CollectionUtils.isEmpty(visibleProjectIds)) {
                rows = List.of();
            } else {
                rows = taskRepository.getKanbanTasksInProjects(visibleProjectIds);
            }
        } else {
            rows = taskRepository.getKanbanTasks(projectId);
        }

        enrichChecklistCounts(rows);
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

    private void enrichChecklistCounts(List<TaskDto> rows) {
        if (CollectionUtils.isEmpty(rows)) return;
        List<UUID> taskIds = rows.stream().map(TaskDto::getId).filter(Objects::nonNull).toList();
        if (CollectionUtils.isEmpty(taskIds)) return;
        Map<UUID, Long> total = taskChecklistRepository.countAllByTaskIds(taskIds).stream()
                .collect(Collectors.toMap(TaskChecklistRepository.TaskChecklistCountRow::getTaskId,
                        TaskChecklistRepository.TaskChecklistCountRow::getCnt, (a, b) -> a));
        Map<UUID, Long> done = taskChecklistRepository.countDoneByTaskIds(taskIds).stream()
                .collect(Collectors.toMap(TaskChecklistRepository.TaskChecklistCountRow::getTaskId,
                        TaskChecklistRepository.TaskChecklistCountRow::getCnt, (a, b) -> a));
        for (TaskDto t : rows) {
            if (t == null || t.getId() == null) continue;
            t.setChecklistTotalCount(total.getOrDefault(t.getId(), 0L));
            t.setChecklistDoneCount(done.getOrDefault(t.getId(), 0L));
        }
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

        Map<UUID, TaskStatusEnum> statusBefore = tasks.stream()
                .collect(Collectors.toMap(Task::getId, Task::getStatus));

        for (Task t : tasks) {
            TaskStatusEnum newStatus = targetStatus.get(t.getId());
            if (newStatus != null && isBlockedByPredecessors(t.getId(), newStatus)) {
                return getResponse400(
                        getMessage(SystemMessage.BAD_REQUEST),
                        Map.of(SystemVariable.STATUS, getMessage(SystemMessage.TASK_PREDECESSOR_NOT_DONE)));
            }
        }

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
        for (Task t : toSave) {
            TaskStatusEnum newStatus = targetStatus.get(t.getId());
            TaskStatusEnum oldBefore = statusBefore.get(t.getId());
            if (newStatus == TaskStatusEnum.DONE && !Objects.equals(oldBefore, TaskStatusEnum.DONE)) {
                completeAllChecklists(t.getId());
                notifySuccessors(t);
            }
        }
        return getResponse200(true, getMessage(SystemMessage.SUCCESS));
    }

    private void completeAllChecklists(UUID taskId) {
        if (taskId == null) {
            return;
        }
        List<TaskChecklist> items = taskChecklistRepository.findByTaskIdAndVoidedFalseOrderBySortOrderAscCreatedDateAsc(taskId);
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        boolean changed = false;
        for (TaskChecklist c : items) {
            if (!Boolean.TRUE.equals(c.getChecked())) {
                c.setChecked(true);
                changed = true;
            }
        }
        if (changed) {
            taskChecklistRepository.saveAll(items);
        }
    }

    /**
     * Gửi thông báo in-app (và email nếu có) cho người được giao công việc.
     * Bỏ qua nếu người nhận trùng người thực hiện thao tác (ví dụ tự giao cho mình).
     */
    private void notifyTaskAssigned(Task task, UUID recipientUserId, UUID actorUserId) {
        if (recipientUserId == null || task == null || task.getId() == null) {
            return;
        }
        if (actorUserId != null && Objects.equals(recipientUserId, actorUserId)) {
            return;
        }
        User recipient = userRepository.findById(recipientUserId).orElse(null);
        if (recipient == null || Boolean.TRUE.equals(recipient.getVoided())) {
            return;
        }
        try {
            List<String> args = List.of(
                    task.getCode() != null ? task.getCode() : "",
                    task.getName() != null ? task.getName() : "");
            String json = objectMapper.writeValueAsString(args);
            Notification n = Notification.builder()
                    .userId(recipientUserId)
                    .type(NotificationTypeEnum.TASK_ASSIGNED)
                    .messageKey(SystemMessage.NOTIFICATION_TASK_ASSIGNED)
                    .messageArgsJson(json)
                    .relatedProjectId(task.getProjectId())
                    .relatedTaskId(task.getId())
                    .voided(false)
                    .build();
            n = notificationRepository.save(n);

            NotificationDelivery inApp = NotificationDelivery.builder()
                    .notificationId(n.getId())
                    .channel(NotificationChannelEnum.IN_APP)
                    .status(NotificationDeliveryStatusEnum.SENT)
                    .attemptCount(0)
                    .sentAt(new java.util.Date())
                    .voided(false)
                    .build();
            notificationDeliveryRepository.save(inApp);

            long unreadCount = notificationRepository.count((root, query, cb) -> cb.and(
                    cb.equal(root.get("userId"), recipientUserId),
                    cb.or(cb.isFalse(root.get("voided")), cb.isNull(root.get("voided"))),
                    cb.isNull(root.get("readAt"))
            ));
            String resolved = getMessage(SystemMessage.NOTIFICATION_TASK_ASSIGNED, args.toArray());
            UserNotificationDto dto = new UserNotificationDto(n, resolved, task.getCode());
            userNotificationStreamService.publishNotification(
                    recipientUserId,
                    dto,
                    unreadCount
            );
            userWebPushService.push(recipientUserId, dto);

            if (StringUtils.hasText(recipient.getEmail()) && task.getProjectId() != null) {
                String link = appProperties.getClientBaseUrl() + "/project/" + task.getProjectId() + "/tasks";
                String modelJson = objectMapper.writeValueAsString(Map.of(
                        "taskCode", task.getCode() != null ? task.getCode() : "",
                        "taskName", task.getName() != null ? task.getName() : "",
                        "link", link,
                        "recipientName", recipient.getFullName() != null ? recipient.getFullName() : "",
                        "recipientEmail", recipient.getEmail()));
                NotificationDelivery email = NotificationDelivery.builder()
                        .notificationId(n.getId())
                        .channel(NotificationChannelEnum.EMAIL)
                        .status(NotificationDeliveryStatusEnum.PENDING)
                        .attemptCount(0)
                        .nextAttemptAt(new java.util.Date())
                        .toAddress(recipient.getEmail())
                        .templateName("mail/task-assigned")
                        .subjectKey(SystemMessage.MAIL_TASK_ASSIGNED_SUBJECT)
                        .modelJson(modelJson)
                        .voided(false)
                        .build();
                notificationDeliveryRepository.save(email);
            }
        } catch (Exception ex) {
            log.warn("Could not create task-assigned notification for user {}", recipientUserId, ex);
        }
    }

    private void notifySuccessors(Task completedTask) {
        if (!TaskStatusEnum.DONE.equals(completedTask.getStatus())) {
            return;
        }
        List<TaskDependency> outs = taskDependencyRepository.findByPredecessorTaskIdAndVoidedFalse(completedTask.getId());
        for (TaskDependency d : outs) {
            Task succ = taskRepository.findById(d.getSuccessorTaskId()).orElse(null);
            if (succ == null || Boolean.TRUE.equals(succ.getVoided())) {
                continue;
            }
            if (succ.getAssignedId() == null) {
                continue;
            }
            User recipient = userRepository.findById(succ.getAssignedId()).orElse(null);
            if (recipient == null || Boolean.TRUE.equals(recipient.getVoided())) {
                continue;
            }
            try {
                List<String> args = List.of(
                        completedTask.getCode() != null ? completedTask.getCode() : "",
                        succ.getCode() != null ? succ.getCode() : "");
                String json = objectMapper.writeValueAsString(args);
                Notification n = Notification.builder()
                        .userId(succ.getAssignedId())
                        .type(NotificationTypeEnum.TASK_PREDECESSOR_DONE)
                        .messageKey(SystemMessage.NOTIFICATION_TASK_PREDECESSOR_DONE)
                        .messageArgsJson(json)
                        .relatedProjectId(succ.getProjectId())
                        .relatedTaskId(succ.getId())
                        .voided(false)
                        .build();
                n = notificationRepository.save(n);

                // IN_APP delivery: đánh dấu SENT ngay để hiển thị
                NotificationDelivery inApp = NotificationDelivery.builder()
                        .notificationId(n.getId())
                        .channel(NotificationChannelEnum.IN_APP)
                        .status(NotificationDeliveryStatusEnum.SENT)
                        .attemptCount(0)
                        .sentAt(new java.util.Date())
                        .voided(false)
                        .build();
                notificationDeliveryRepository.save(inApp);

                long unreadCount = notificationRepository.count((root, query, cb) -> cb.and(
                        cb.equal(root.get("userId"), succ.getAssignedId()),
                        cb.or(cb.isFalse(root.get("voided")), cb.isNull(root.get("voided"))),
                        cb.isNull(root.get("readAt"))
                ));
                String resolved = getMessage(SystemMessage.NOTIFICATION_TASK_PREDECESSOR_DONE, args.toArray());
                UserNotificationDto dto = new UserNotificationDto(n, resolved, succ.getCode());
                userNotificationStreamService.publishNotification(
                        succ.getAssignedId(),
                        dto,
                        unreadCount
                );
                userWebPushService.push(succ.getAssignedId(), dto);

                // EMAIL delivery: để job quét và gửi (Thymeleaf template)
                String link = appProperties.getClientBaseUrl() + "/project/" + succ.getProjectId() + "/tasks";
                String modelJson = objectMapper.writeValueAsString(java.util.Map.of(
                        "predCode", completedTask.getCode(),
                        "succCode", succ.getCode(),
                        "predName", completedTask.getName(),
                        "succName", succ.getName(),
                        "link", link,
                        "recipientName", recipient.getFullName(),
                        "recipientEmail", recipient.getEmail()
                ));
                NotificationDelivery email = NotificationDelivery.builder()
                        .notificationId(n.getId())
                        .channel(NotificationChannelEnum.EMAIL)
                        .status(NotificationDeliveryStatusEnum.PENDING)
                        .attemptCount(0)
                        .nextAttemptAt(new java.util.Date())
                        .toAddress(recipient.getEmail())
                        .templateName("mail/task-predecessor-done")
                        .subjectKey(SystemMessage.MAIL_TASK_PREDECESSOR_DONE_SUBJECT)
                        .modelJson(modelJson)
                        .voided(false)
                        .build();
                notificationDeliveryRepository.save(email);
            } catch (Exception ex) {
                log.warn("Could not create notification outbox for successor task {}", d.getSuccessorTaskId(), ex);
            }
        }
    }

    private boolean requiresPredecessorsDone(TaskStatusEnum newStatus) {
        if (newStatus == null) {
            return false;
        }
        return newStatus == TaskStatusEnum.IN_PROGRESS
                || newStatus == TaskStatusEnum.REVIEW
                || newStatus == TaskStatusEnum.TESTING;
    }

    private boolean isBlockedByPredecessors(UUID taskId, TaskStatusEnum newStatus) {
        if (!requiresPredecessorsDone(newStatus)) {
            return false;
        }
        List<TaskDependency> preds = taskDependencyRepository.findBySuccessorTaskIdAndVoidedFalse(taskId);
        for (TaskDependency d : preds) {
            Task p = taskRepository.findById(d.getPredecessorTaskId()).orElse(null);
            if (p == null || Boolean.TRUE.equals(p.getVoided())) {
                return true;
            }
            if (!TaskStatusEnum.DONE.equals(p.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private void voidTaskRelations(UUID taskId) {
        List<TaskDependency> deps = taskDependencyRepository.findAllActiveByPredecessorOrSuccessor(taskId);
        for (TaskDependency d : deps) {
            d.setVoided(true);
        }
        if (!deps.isEmpty()) {
            taskDependencyRepository.saveAll(deps);
        }
        List<TaskChecklist> checklists = taskChecklistRepository.findByTaskId(taskId);
        for (TaskChecklist c : checklists) {
            c.setVoided(true);
        }
        if (!checklists.isEmpty()) {
            taskChecklistRepository.saveAll(checklists);
        }
    }

    private TaskDto buildTaskDtoWithRelations(UUID taskId) {
        Task entity = taskRepository.findById(taskId).orElse(null);
        if (entity == null) {
            return null;
        }
        TaskDto dto = new TaskDto(entity);
        dto.setChecklists(loadChecklistDtos(taskId));
        dto.setPredecessors(loadPredecessorRefs(taskId));
        dto.setPredecessorTaskIds(
                dto.getPredecessors() == null
                        ? List.of()
                        : dto.getPredecessors().stream().map(TaskRefDto::getId).filter(Objects::nonNull).toList());
        return dto;
    }

    private List<TaskChecklistDto> loadChecklistDtos(UUID taskId) {
        return taskChecklistRepository.findByTaskIdAndVoidedFalseOrderBySortOrderAscCreatedDateAsc(taskId).stream()
                .map(TaskChecklistDto::new)
                .toList();
    }

    private List<TaskRefDto> loadPredecessorRefs(UUID successorTaskId) {
        List<TaskRefDto> refs = new ArrayList<>();
        for (TaskDependency d : taskDependencyRepository.findBySuccessorTaskIdAndVoidedFalse(successorTaskId)) {
            taskRepository.findById(d.getPredecessorTaskId()).ifPresent(pt -> {
                if (pt.getVoided() == null || !pt.getVoided()) {
                    refs.add(new TaskRefDto(pt));
                }
            });
        }
        return refs;
    }

    private HashMap<String, String> validateChecklists(List<TaskChecklistDto> items) {
        HashMap<String, String> errors = new HashMap<>();
        if (items == null) {
            return errors;
        }
        int i = 0;
        for (TaskChecklistDto it : items) {
            if (it == null || !StringUtils.hasText(it.getTitle())) {
                errors.put(SystemVariable.CHECKLISTS + "[" + i + "]." + SystemVariable.TITLE, getMessage(SystemMessage.VALIDATION_NOT_BLANK));
            }
            i++;
        }
        return errors;
    }

    private void applyChecklistEstimatedHoursIfAny(Task entity, List<TaskChecklistDto> items) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        entity.setEstimatedHours(sumChecklistHours(items));
    }

    private BigDecimal sumChecklistHours(List<TaskChecklistDto> items) {
        return items.stream()
                .filter(Objects::nonNull)
                .map(TaskChecklistDto::getEstimatedHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void replaceChecklists(UUID taskId, List<TaskChecklistDto> items) {
        List<TaskChecklist> existing = taskChecklistRepository.findByTaskId(taskId);
        for (TaskChecklist c : existing) {
            c.setVoided(true);
        }
        if (!existing.isEmpty()) {
            taskChecklistRepository.saveAll(existing);
        }
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        int order = 0;
        List<TaskChecklist> toSave = new ArrayList<>();
        for (TaskChecklistDto dto : items) {
            if (dto == null || !StringUtils.hasText(dto.getTitle())) {
                continue;
            }
            TaskChecklist row = TaskChecklist.builder()
                    .taskId(taskId)
                    .title(dto.getTitle().trim())
                    .checked(Boolean.TRUE.equals(dto.getChecked()))
                    .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : order)
                    .estimatedHours(dto.getEstimatedHours())
                    .voided(false)
                    .build();
            toSave.add(row);
            order++;
        }
        if (!toSave.isEmpty()) {
            taskChecklistRepository.saveAll(toSave);
        }
    }

    private List<UUID> normalizePredecessorIds(List<UUID> predecessorIds) {
        if (CollectionUtils.isEmpty(predecessorIds)) {
            return List.of();
        }
        return predecessorIds.stream().filter(Objects::nonNull).distinct().toList();
    }

    private void replacePredecessors(UUID taskId, UUID projectId, List<UUID> predecessorIds) {
        List<TaskDependency> prevActive = taskDependencyRepository.findBySuccessorTaskIdAndVoidedFalse(taskId);
        for (TaskDependency d : prevActive) {
            d.setVoided(true);
        }
        if (!prevActive.isEmpty()) {
            taskDependencyRepository.saveAll(prevActive);
        }
        List<UUID> preds = normalizePredecessorIds(predecessorIds);
        if (preds.isEmpty()) {
            return;
        }
        List<TaskDependency> created = new ArrayList<>();
        for (UUID predId : preds) {
            if (predId == null) {
                continue;
            }
            TaskDependency d = TaskDependency.builder()
                    .predecessorTaskId(predId)
                    .successorTaskId(taskId)
                    .voided(false)
                    .build();
            created.add(d);
        }
        if (!created.isEmpty()) {
            taskDependencyRepository.saveAll(created);
        }
    }

    private HashMap<String, String> validatePredecessorTasks(UUID projectId, UUID successorId, List<UUID> predecessorIds) {
        HashMap<String, String> errors = new HashMap<>();
        if (CollectionUtils.isEmpty(predecessorIds) || projectId == null) {
            return errors;
        }
        int i = 0;
        for (UUID pid : predecessorIds) {
            if (pid == null) {
                i++;
                continue;
            }
            if (Objects.nonNull(successorId) && Objects.equals(pid, successorId)) {
                errors.put(SystemVariable.PREDECESSOR_TASK_IDS + "[" + i + "]", getMessage(SystemMessage.TASK_DEPENDENCY_SELF));
            } else {
                Task t = taskRepository.findById(pid).orElse(null);
                if (Objects.isNull(t) || Boolean.TRUE.equals(t.getVoided()) || !Objects.equals(t.getProjectId(), projectId)) {
                    errors.put(SystemVariable.PREDECESSOR_TASK_IDS + "[" + i + "]", getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.TASK)));
                }
            }
            i++;
        }
        return errors;
    }

    private HashMap<String, String> validateDependencyGraph(UUID projectId, UUID successorId, List<UUID> predecessorIds) {
        HashMap<String, String> errors = new HashMap<>();
        if (CollectionUtils.isEmpty(predecessorIds) || projectId == null || successorId == null) {
            return errors;
        }
        List<TaskDependency> all = taskDependencyRepository.findAllActiveInProject(projectId);
        Map<UUID, List<UUID>> adj = new HashMap<>();
        for (TaskDependency d : all) {
            if (Boolean.TRUE.equals(d.getVoided())) {
                continue;
            }
            if (Objects.equals(d.getSuccessorTaskId(), successorId)) {
                continue;
            }
            adj.computeIfAbsent(d.getPredecessorTaskId(), k -> new ArrayList<>()).add(d.getSuccessorTaskId());
        }
        for (UUID p : predecessorIds) {
            if (p == null || Objects.equals(p, successorId)) {
                continue;
            }
            adj.computeIfAbsent(p, k -> new ArrayList<>()).add(successorId);
        }
        for (UUID p : predecessorIds) {
            if (p == null || Objects.equals(p, successorId)) {
                continue;
            }
            if (hasPath(adj, successorId, p, new HashSet<>())) {
                errors.put(SystemVariable.PREDECESSOR_TASK_IDS, getMessage(SystemMessage.TASK_DEPENDENCY_CYCLE));
                break;
            }
        }
        return errors;
    }

    private boolean hasPath(Map<UUID, List<UUID>> adj, UUID start, UUID target, Set<UUID> visited) {
        if (Objects.equals(start, target)) {
            return true;
        }
        if (!visited.add(start)) {
            return false;
        }
        List<UUID> next = adj.getOrDefault(start, List.of());
        for (UUID n : next) {
            if (hasPath(adj, n, target, visited)) {
                return true;
            }
        }
        return false;
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
                ";reporterId=" + (t.getReporterId() != null ? t.getReporterId() : "") +
                ";reviewerId=" + (t.getReviewerId() != null ? t.getReviewerId() : "") +
                ";parentTaskId=" + (t.getParentTaskId() != null ? t.getParentTaskId() : "") +
                ";dueDate=" + (t.getDueDate() != null ? t.getDueDate().getTime() : "") +
                ";blockedReason=" + nullSafe(t.getBlockedReason()) +
                ";taskCategory=" + nullSafe(t.getTaskCategory()) +
                ";storyPoint=" + (t.getStoryPoint() != null ? t.getStoryPoint() : "") +
                ";label=" + nullSafe(t.getLabel());
    }

    private UUID resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).map(User::getId).orElse(null);
    }

    private HashMap<String, String> validateTaskProjectAndMembers(
            UUID projectId,
            UUID assignedId,
            UUID reporterId,
            UUID reviewerId,
            UUID parentTaskId,
            UUID currentTaskId) {
        HashMap<String, String> errors = new HashMap<>();
        if (Objects.nonNull(projectId)) {
            Project project = projectRepository.findById(projectId).orElse(null);
            if (Objects.isNull(project) || Boolean.TRUE.equals(project.getVoided())) {
                errors.put(SystemVariable.PROJECT_ID, getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.PROJECT)));
            } else {
                if (Objects.nonNull(assignedId)
                        && !projectMemberRepository.existsActiveMember(projectId, assignedId)) {
                    errors.put(SystemVariable.ASSIGNED_ID, getMessage(SystemMessage.NOT_PROJECT_MEMBER));
                }
                if (Objects.nonNull(reporterId)
                        && !projectMemberRepository.existsActiveMember(projectId, reporterId)) {
                    errors.put(SystemVariable.REPORTER_ID, getMessage(SystemMessage.NOT_PROJECT_MEMBER));
                }
                if (Objects.nonNull(reviewerId)
                        && !projectMemberRepository.existsActiveMember(projectId, reviewerId)) {
                    errors.put(SystemVariable.REVIEWER_ID, getMessage(SystemMessage.NOT_PROJECT_MEMBER));
                }
            }
        }
        if (Objects.nonNull(parentTaskId)) {
            if (Objects.nonNull(currentTaskId) && Objects.equals(parentTaskId, currentTaskId)) {
                errors.put(SystemVariable.PARENT_TASK_ID, getMessage(SystemMessage.INVALID_PARENT_TASK));
            } else {
                Task parent = taskRepository.findById(parentTaskId).orElse(null);
                if (Objects.isNull(parent) || Boolean.TRUE.equals(parent.getVoided())) {
                    errors.put(SystemVariable.PARENT_TASK_ID, getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.TASK)));
                } else if (Objects.isNull(projectId) || !Objects.equals(parent.getProjectId(), projectId)) {
                    errors.put(SystemVariable.PARENT_TASK_ID, getMessage(SystemMessage.INVALID_PARENT_TASK));
                }
            }
        }
        return errors;
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private boolean isProjectManager(UUID projectId, UUID userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        return projectMemberRepository.existsActiveMemberWithRole(projectId, userId, "PROJECT_MANAGER");
    }
}
