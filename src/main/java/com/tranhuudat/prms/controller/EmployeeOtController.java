package com.tranhuudat.prms.controller;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.employee_ot.EmployeeOtDto;
import com.tranhuudat.prms.dto.employee_ot.EmployeeOtMonthlyReportRequest;
import com.tranhuudat.prms.dto.employee_ot.EmployeeOtSearchRequest;
import com.tranhuudat.prms.service.EmployeeOtService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employee-ots")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeOtController {

    EmployeeOtService employeeOtService;

    @PostMapping
    public ResponseEntity<BaseResponse> create(@RequestBody EmployeeOtDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeOtService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id, @RequestBody EmployeeOtDto request) {
        return ResponseEntity.ok(employeeOtService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeOtService.delete(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeOtService.getById(id));
    }

    @PostMapping("/page")
    public ResponseEntity<BaseResponse> getPage(@RequestBody EmployeeOtSearchRequest request) {
        return ResponseEntity.ok(employeeOtService.getPage(request));
    }

    @PostMapping("/report/monthly")
    public ResponseEntity<byte[]> exportMonthlyReport(@RequestBody EmployeeOtMonthlyReportRequest request) {
        byte[] bytes = employeeOtService.exportMonthlyReport(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ot-report-" + request.getMonth() + ".docx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(bytes);
    }
}
