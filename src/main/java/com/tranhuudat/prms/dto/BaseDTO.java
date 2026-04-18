package com.tranhuudat.prms.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author DatNuclear 04/09/2026 05:00 PM
 * @project prms
 * @package com.tranhuudat.prms.dto
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseDTO {
    private UUID id;
    private String name;
    private String description;
    private String shortDescription;
    private String code;
}
