package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.report.ReportFilterRequest;

public interface ReportService {

    BaseResponse loadReport(ReportFilterRequest request);
}
