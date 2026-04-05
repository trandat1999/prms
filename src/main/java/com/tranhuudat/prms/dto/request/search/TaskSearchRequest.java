package com.tranhuudat.prms.dto.request.search;

import com.tranhuudat.prms.dto.SearchRequest;
import com.tranhuudat.prms.enums.TaskStatusEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskSearchRequest extends SearchRequest {
    UUID projectId;
    UUID assignedUserId;
    LocalDate dueDate;
    TaskStatusEnum status;
}
