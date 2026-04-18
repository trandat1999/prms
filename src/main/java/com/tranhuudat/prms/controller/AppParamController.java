package com.tranhuudat.prms.controller;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.app_param.AppParamDto;
import com.tranhuudat.prms.dto.app_param.AppParamSearchRequest;
import com.tranhuudat.prms.service.AppParamService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/app-params")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppParamController {
    AppParamService appParamService;

    @PostMapping
    public ResponseEntity<BaseResponse> create(@RequestBody AppParamDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appParamService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id, @RequestBody AppParamDto request) {
        return ResponseEntity.ok(appParamService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(appParamService.delete(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(appParamService.getById(id));
    }

    @PostMapping("/page")
    public ResponseEntity<BaseResponse> getPage(@RequestBody AppParamSearchRequest request) {
        return ResponseEntity.ok(appParamService.getPage(request));
    }
}

