package com.tranhuudat.prms.dto.autocomplete;

import com.tranhuudat.prms.dto.SearchRequest;
import com.tranhuudat.prms.enums.ProjectStatusEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

/**
 * Request dùng cho các API autocomplete.
 * Kế thừa chuẩn phân trang/keyword từ {@link SearchRequest}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutocompleteSearchRequest extends SearchRequest {
    /** Dùng cho autocomplete thành viên trong một dự án. */
    UUID projectId;

    /** Dùng cho autocomplete project theo trạng thái. */
    ProjectStatusEnum projectStatus;
}

