package com.tranhuudat.prms.dto.autocomplete;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillAutocompleteDto {
    UUID id;
    String code;
    String name;
    String category;
}

