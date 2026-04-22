package com.tranhuudat.prms.controller;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.resource_allocation.ResourceAllocationDto;
import com.tranhuudat.prms.dto.resource_allocation.ResourceAllocationSearchRequest;
import com.tranhuudat.prms.service.ResourceAllocationService;
import com.tranhuudat.prms.util.DateUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
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

    @PostMapping(
            value = "/export/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportExcel(@RequestBody ResourceAllocationSearchRequest request) {
        try {
            byte[] body = resourceAllocationService.exportResourceEmployeeExcel(request);
            String ym =
                    DateUtil.toLocalDate(request.getMonth()).format(DateTimeFormatter.ofPattern("MM-yyyy"));
            ContentDisposition disposition = ContentDisposition.attachment()
                    .filename("phan-bo-nguon-luc-" + ym + ".xlsx", StandardCharsets.UTF_8)
                    .build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(body);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
