package com.tranhuudat.prms.dto.request.search;

import com.tranhuudat.prms.dto.SearchRequest;
import com.tranhuudat.prms.enums.ProjectStatusEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectSearchRequest extends SearchRequest {
    UUID managerId;
    LocalDate startDate;
    LocalDate endDate;
    ProjectStatusEnum status;
}
