package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.employee_ot.EmployeeOtDto;
import com.tranhuudat.prms.dto.employee_ot.EmployeeOtSearchRequest;
import com.tranhuudat.prms.dto.employee_ot.EmployeeOtMonthlyReportRequest;

import java.util.UUID;

public interface EmployeeOtService {

    BaseResponse create(EmployeeOtDto request);

    BaseResponse update(UUID id, EmployeeOtDto request);

    BaseResponse delete(UUID id);

    BaseResponse getById(UUID id);

    BaseResponse getPage(EmployeeOtSearchRequest request);

    byte[] exportMonthlyReport(EmployeeOtMonthlyReportRequest request);
}
