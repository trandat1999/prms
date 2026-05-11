package com.tranhuudat.prms.dto.skill;

import com.tranhuudat.prms.dto.SearchRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SkillSearchRequest extends SearchRequest {
    String category;
}

