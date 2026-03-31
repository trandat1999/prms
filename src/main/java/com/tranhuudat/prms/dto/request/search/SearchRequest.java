package com.tranhuudat.prms.dto.request.search;

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
    Integer pageIndex = 1;
    Integer pageSize = 10;
    String keyword;
    String sortBy = "name";
    String direction = "ASC";
}
