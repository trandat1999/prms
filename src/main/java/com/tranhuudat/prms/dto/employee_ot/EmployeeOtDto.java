package com.tranhuudat.prms.dto.employee_ot;

import com.tranhuudat.prms.entity.EmployeeOt;
import com.tranhuudat.prms.enums.EmployeeOtStatusEnum;
import com.tranhuudat.prms.enums.EmployeeOtTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeOtDto {
    UUID id;

    @NotNull
    UUID userId;
    String userDisplay;

    UUID projectId;
    String projectDisplay;

    @NotNull
    Date otDate;

    Date startTime;
    Date endTime;

    BigDecimal otHours;

    @NotNull
    EmployeeOtTypeEnum otType;
    BigDecimal otTypeCoefficient;
    BigDecimal weightedOtHours;

    String reason;

    EmployeeOtStatusEnum status;

    UUID approvedBy;
    String approvedByDisplay;
    Date approvedDate;

    public EmployeeOtDto(EmployeeOt entity) {
        if (Objects.isNull(entity)) {
            return;
        }
        BeanUtils.copyProperties(
                entity,
                this,
                "user",
                "project",
                "approver",
                "otTypeCoefficient",
                "weightedOtHours",
                "userDisplay",
                "projectDisplay",
                "approvedByDisplay");
        if (entity.getOtType() != null) {
            this.otTypeCoefficient = entity.getOtType().getCoefficient();
            if (entity.getOtHours() != null) {
                this.weightedOtHours = entity.getOtHours().multiply(entity.getOtType().getCoefficient());
            }
        }
        if (entity.getUser() != null) {
            String u = entity.getUser().getUsername();
            String f = entity.getUser().getFullName();
            this.userDisplay = (u != null ? u : "") + " — " + (f != null ? f : "");
        }
        if (entity.getProject() != null) {
            String c = entity.getProject().getCode();
            String n = entity.getProject().getName();
            this.projectDisplay = (c != null ? c : "") + " — " + (n != null ? n : "");
        }
        if (entity.getApprover() != null) {
            String u = entity.getApprover().getUsername();
            String f = entity.getApprover().getFullName();
            this.approvedByDisplay = (u != null ? u : "") + " — " + (f != null ? f : "");
        }
    }
}
