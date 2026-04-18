package com.tranhuudat.prms.controller;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.resource_allocation.ResourceAllocationDto;
import com.tranhuudat.prms.dto.resource_allocation.ResourceAllocationSearchRequest;
import com.tranhuudat.prms.service.ResourceAllocationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resource-allocations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ResourceAllocationController {

    ResourceAllocationService resourceAllocationService;

    @PostMapping
    public ResponseEntity<BaseResponse> create(@RequestBody ResourceAllocationDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceAllocationService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id, @RequestBody ResourceAllocationDto request) {
        return ResponseEntity.ok(resourceAllocationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceAllocationService.delete(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceAllocationService.getById(id));
    }

    @PostMapping("/page")
    public ResponseEntity<BaseResponse> getPage(@RequestBody ResourceAllocationSearchRequest request) {
        return ResponseEntity.ok(resourceAllocationService.getPage(request));
    }
}
