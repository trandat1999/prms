package com.tranhuudat.prms.dto.task;

import com.tranhuudat.prms.util.SystemMessage;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskChecklistToggleRequest {
    @NotNull(message = SystemMessage.VALIDATION_NOTNULL)
    Boolean checked;
}

