package com.tranhuudat.prms.dto.project_member;

import com.tranhuudat.prms.dto.SearchRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectMemberSearchRequest extends SearchRequest {
    UUID projectId;
    UUID userId;
    String roleInProject;
    Boolean active;
}
