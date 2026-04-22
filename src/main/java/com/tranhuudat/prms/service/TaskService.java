package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.task.TaskAssignRequest;
import com.tranhuudat.prms.dto.task.TaskChecklistToggleRequest;
import com.tranhuudat.prms.dto.task.TaskChecklistDto;
import com.tranhuudat.prms.dto.task.TaskKanbanBoardUpdateRequest;
import com.tranhuudat.prms.dto.task.TaskDto;
import com.tranhuudat.prms.dto.task.TaskSearchRequest;
import com.tranhuudat.prms.dto.task.TaskStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    BaseResponse create(TaskDto request);
    BaseResponse update(UUID id, TaskDto request);
    BaseResponse delete(UUID id);
    BaseResponse getById(UUID id);
    BaseResponse getPage(TaskSearchRequest request);
    BaseResponse assign(UUID id, TaskAssignRequest request);
    BaseResponse updateStatus(UUID id, TaskStatusUpdateRequest request);
    BaseResponse getLogs(UUID id);
    BaseResponse getKanbanBoard(UUID projectId);
    BaseResponse updateKanbanBoard(TaskKanbanBoardUpdateRequest request);

    BaseResponse getChecklists(UUID taskId);
    BaseResponse toggleChecklist(UUID taskId, UUID checklistId, TaskChecklistToggleRequest request);
}

