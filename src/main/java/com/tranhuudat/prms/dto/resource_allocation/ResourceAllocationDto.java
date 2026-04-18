package com.tranhuudat.prms.dto.resource_allocation;

import com.tranhuudat.prms.entity.ResourceAllocation;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResourceAllocationDto {
    UUID id;

    @NotNull
    UUID userId;
    String userDisplay;

    @NotBlank
    String role;

    @NotNull
    Date month;

    Date startDate;
    Date endDate;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    BigDecimal allocationPercent;

    public ResourceAllocationDto(ResourceAllocation entity) {
        if (Objects.nonNull(entity)) {
            BeanUtils.copyProperties(entity, this, "user", "resourceMonth");
            this.month = entity.getResourceMonth();
            if (entity.getUser() != null) {
                String u = entity.getUser().getUsername();
                String f = entity.getUser().getFullName();
                this.userDisplay = (u != null ? u : "") + " — " + (f != null ? f : "");
            }
        }
    }
}
