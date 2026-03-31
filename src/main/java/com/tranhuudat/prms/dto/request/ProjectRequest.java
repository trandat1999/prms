package com.tranhuudat.prms.dto.request;

import com.tranhuudat.prms.enums.PriorityEnum;
import com.tranhuudat.prms.enums.ProjectStatusEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectRequest {
    @NotBlank(message = "NAME_INVALID")
    String name;

    String description;
    String shortDescription;

    @NotBlank(message = "CODE_INVALID")
    String code;

    UUID managerId;
    PriorityEnum priority;
    ProjectStatusEnum status;
    Double progressPercentage;
    LocalDate startDate;
    LocalDate endDate;
}
