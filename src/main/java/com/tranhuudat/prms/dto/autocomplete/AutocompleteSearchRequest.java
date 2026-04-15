package com.tranhuudat.prms.dto.autocomplete;

import com.tranhuudat.prms.dto.SearchRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

/**
 * Request dùng cho các API autocomplete.
 * Kế thừa chuẩn phân trang/keyword từ {@link SearchRequest}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutocompleteSearchRequest extends SearchRequest {
}

