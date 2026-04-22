package com.tranhuudat.prms.controller;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.task.TaskAssignRequest;
import com.tranhuudat.prms.dto.task.TaskChecklistToggleRequest;
import com.tranhuudat.prms.dto.task.TaskKanbanBoardUpdateRequest;
import com.tranhuudat.prms.dto.task.TaskDto;
import com.tranhuudat.prms.dto.task.TaskSearchRequest;
import com.tranhuudat.prms.dto.task.TaskStatusUpdateRequest;
import com.tranhuudat.prms.service.TaskService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskController {

    TaskService taskService;

    @PostMapping
    public ResponseEntity<BaseResponse> create(@RequestBody TaskDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id, @RequestBody TaskDto request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.delete(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @PostMapping("/page")
    public ResponseEntity<BaseResponse> getPage(@RequestBody TaskSearchRequest request) {
        return ResponseEntity.ok(taskService.getPage(request));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<BaseResponse> assign(@PathVariable UUID id, @RequestBody TaskAssignRequest request) {
        return ResponseEntity.ok(taskService.assign(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BaseResponse> updateStatus(@PathVariable UUID id, @RequestBody TaskStatusUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateStatus(id, request));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<BaseResponse> getLogs(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getLogs(id));
    }

    @GetMapping("/{id}/checklists")
    public ResponseEntity<BaseResponse> getChecklists(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getChecklists(id));
    }

    @PatchMapping("/{taskId}/checklists/{checklistId}")
    public ResponseEntity<BaseResponse> toggleChecklist(
            @PathVariable UUID taskId,
            @PathVariable UUID checklistId,
            @RequestBody TaskChecklistToggleRequest request
    ) {
        return ResponseEntity.ok(taskService.toggleChecklist(taskId, checklistId, request));
    }

    @GetMapping("/kanban/board")
    public ResponseEntity<BaseResponse> getKanbanBoard(@RequestParam(required = false) UUID projectId) {
        return ResponseEntity.ok(taskService.getKanbanBoard(projectId));
    }

    @PutMapping("/kanban/board")
    public ResponseEntity<BaseResponse> updateKanbanBoard(@RequestBody TaskKanbanBoardUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateKanbanBoard(request));
    }
}
