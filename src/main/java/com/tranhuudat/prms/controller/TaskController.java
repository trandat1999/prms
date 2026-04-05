package com.tranhuudat.prms.controller;

import com.tranhuudat.prms.dto.request.TaskRequest;
import com.tranhuudat.prms.dto.request.search.TaskSearchRequest;
import com.tranhuudat.prms.dto.response.ApiResponse;
import com.tranhuudat.prms.dto.response.PageResponse;
import com.tranhuudat.prms.dto.response.TaskResponse;
import com.tranhuudat.prms.service.TaskService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskController {
    TaskService taskService;

    @PostMapping
    public ApiResponse<TaskResponse> create(@RequestBody @Valid TaskRequest request) {
        return ApiResponse.<TaskResponse>builder()
                .result(taskService.create(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<TaskResponse> update(@PathVariable UUID id, @RequestBody @Valid TaskRequest request) {
        return ApiResponse.<TaskResponse>builder()
                .result(taskService.update(id, request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskResponse> getById(@PathVariable UUID id) {
        return ApiResponse.<TaskResponse>builder()
                .result(taskService.getById(id))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return ApiResponse.<Void>builder()
                .message("Task deleted successfully")
                .build();
    }

    @PostMapping("/search")
    public ApiResponse<PageResponse> getAll(@RequestBody TaskSearchRequest request) {
        return ApiResponse.<PageResponse>builder()
                .result(taskService.getAll(request))
                .build();
    }
}
