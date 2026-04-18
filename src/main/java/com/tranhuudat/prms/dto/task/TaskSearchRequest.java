package com.tranhuudat.prms.dto.task;

import com.tranhuudat.prms.dto.SearchRequest;
import com.tranhuudat.prms.enums.TaskStatusEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskSearchRequest extends SearchRequest {
    UUID projectId;
    UUID assignedId;
    TaskStatusEnum status;
    String type;
}

