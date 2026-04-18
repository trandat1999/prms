package com.tranhuudat.prms.dto.user;

import com.tranhuudat.prms.dto.SearchRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

/**
 * Search request cho màn hình quản lý người dùng.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSearchRequest extends SearchRequest {
    /**
     * Lọc theo trạng thái enabled. Null = không lọc.
     */
    Boolean enabled;
}

