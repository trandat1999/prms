package com.tranhuudat.prms.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author DatNuclear 03/31/2026 04:01 PM
 * @project prms
 * @package com.tranhuudat.prms.dto.request.search
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchRequest {
    protected Boolean voided;
    protected String keyword;
    protected Integer pageSize;
    protected Integer pageIndex;
}
