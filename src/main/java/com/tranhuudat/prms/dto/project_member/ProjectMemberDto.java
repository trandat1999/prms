package com.tranhuudat.prms.dto.project_member;

import com.tranhuudat.prms.entity.ProjectMember;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
public class ProjectMemberDto {
    UUID id;

    @NotNull
    UUID projectId;
    String projectDisplay;

    @NotNull
    UUID userId;
    String userDisplay;

    @NotNull
    String roleInProject;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    BigDecimal allocationPercent;

    Boolean isLead;
    Date startDate;
    Date endDate;
    Boolean active;

    public ProjectMemberDto(ProjectMember entity) {
        if (Objects.nonNull(entity)) {
            BeanUtils.copyProperties(entity, this, "project", "user");
            if (entity.getProject() != null) {
                String c = entity.getProject().getCode();
                String n = entity.getProject().getName();
                this.projectDisplay = (c != null ? c : "") + " - " + (n != null ? n : "");
            }
            if (entity.getUser() != null) {
                String u = entity.getUser().getUsername();
                String f = entity.getUser().getFullName();
                this.userDisplay = (u != null ? u : "") + " - " + (f != null ? f : "");
            }
        }
    }
}
