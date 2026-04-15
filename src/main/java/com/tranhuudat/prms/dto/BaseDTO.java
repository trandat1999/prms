package com.tranhuudat.prms.dto;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

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
