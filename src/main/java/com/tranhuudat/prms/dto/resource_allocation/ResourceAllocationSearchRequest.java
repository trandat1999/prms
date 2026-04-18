package com.tranhuudat.prms.dto.resource_allocation;

import com.tranhuudat.prms.dto.SearchRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResourceAllocationSearchRequest extends SearchRequest {
    UUID userId;
    String role;
    Date month;
    Integer year;
    Integer monthYear;
}
