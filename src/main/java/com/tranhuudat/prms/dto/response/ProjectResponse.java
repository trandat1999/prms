package com.tranhuudat.prms.dto.response;

import com.tranhuudat.prms.enums.PriorityEnum;
import com.tranhuudat.prms.enums.ProjectStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectResponse {
    UUID id;
    String name;
    String description;
    String shortDescription;
    String code;
    UUID managerId;
    String managerFullName;
    PriorityEnum priority;
    ProjectStatusEnum status;
    Double progressPercentage;
    LocalDate startDate;
    LocalDate endDate;
}
