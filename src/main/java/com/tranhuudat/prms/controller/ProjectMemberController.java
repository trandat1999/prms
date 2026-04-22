package com.tranhuudat.prms.controller;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.project_member.ProjectMemberDto;
import com.tranhuudat.prms.dto.project_member.ProjectMemberSearchRequest;
import com.tranhuudat.prms.service.ProjectMemberService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/project-members")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectMemberController {

    ProjectMemberService projectMemberService;

    @PostMapping
    public ResponseEntity<BaseResponse> create(@RequestBody ProjectMemberDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectMemberService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id, @RequestBody ProjectMemberDto request) {
        return ResponseEntity.ok(projectMemberService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(projectMemberService.delete(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectMemberService.getById(id));
    }

    @PostMapping("/page")
    public ResponseEntity<BaseResponse> getPage(@RequestBody ProjectMemberSearchRequest request) {
        return ResponseEntity.ok(projectMemberService.getPage(request));
    }
}
