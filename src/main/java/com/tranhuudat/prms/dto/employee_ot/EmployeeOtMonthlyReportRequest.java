package com.tranhuudat.prms.dto.employee_ot;

import com.tranhuudat.prms.enums.EmployeeOtStatusEnum;
import com.tranhuudat.prms.enums.EmployeeOtTypeEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeOtMonthlyReportRequest {
    String keyword;
    UUID userId;
    UUID projectId;
    EmployeeOtStatusEnum status;
    EmployeeOtTypeEnum otType;

    /**
     * Format: yyyy-MM
     */
    @NotBlank
    String month;
}

