package com.tranhuudat.prms.controller;

import com.tranhuudat.prms.dto.request.ProjectRequest;
import com.tranhuudat.prms.dto.request.search.ProjectSearchRequest;
import com.tranhuudat.prms.dto.response.ApiResponse;
import com.tranhuudat.prms.dto.response.PageResponse;
import com.tranhuudat.prms.dto.response.ProjectResponse;
import com.tranhuudat.prms.service.ProjectService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectController {
    ProjectService projectService;

    @PostMapping
    public ApiResponse<ProjectResponse> create(@RequestBody @Valid ProjectRequest request) {
        return ApiResponse.<ProjectResponse>builder()
                .result(projectService.create(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ProjectResponse> update(@PathVariable UUID id, @RequestBody @Valid ProjectRequest request) {
        return ApiResponse.<ProjectResponse>builder()
                .result(projectService.update(id, request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectResponse> getById(@PathVariable UUID id) {
        return ApiResponse.<ProjectResponse>builder()
                .result(projectService.getById(id))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        projectService.delete(id);
        return ApiResponse.<Void>builder()
                .message("Project deleted successfully")
                .build();
    }

    @PostMapping("/search")
    public ApiResponse<PageResponse<ProjectResponse>> getAll(@RequestBody ProjectSearchRequest request) {
        return ApiResponse.<PageResponse<ProjectResponse>>builder()
                .result(projectService.getAll(request))
                .build();
    }
}
