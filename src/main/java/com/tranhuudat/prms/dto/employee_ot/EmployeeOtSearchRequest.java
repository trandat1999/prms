package com.tranhuudat.prms.dto.employee_ot;

import com.tranhuudat.prms.dto.SearchRequest;
import com.tranhuudat.prms.enums.EmployeeOtStatusEnum;
import com.tranhuudat.prms.enums.EmployeeOtTypeEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeOtSearchRequest extends SearchRequest {
    UUID userId;
    UUID projectId;
    EmployeeOtStatusEnum status;
    EmployeeOtTypeEnum otType;
    Date otDateFrom;
    Date otDateTo;
}
