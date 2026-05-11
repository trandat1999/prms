package com.tranhuudat.prms.dto.user;

import com.tranhuudat.prms.entity.Skill;
import com.tranhuudat.prms.entity.UserSkill;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSkillDto {
    UUID id;
    UUID userId;
    UUID skillId;
    String skillCode;
    String skillName;
    String skillCategory;
    String level;
    BigDecimal experienceYear;
    LocalDate lastUsedDate;
    Boolean isPrimary;

    public UserSkillDto(UserSkill entity) {
        if (Objects.nonNull(entity)) {
            this.id = entity.getId();
            this.userId = entity.getUserId();
            this.skillId = entity.getSkillId();
            this.level = entity.getLevel();
            this.experienceYear = entity.getExperienceYear();
            this.lastUsedDate = entity.getLastUsedDate();
            this.isPrimary = entity.getIsPrimary();
            Skill skill = entity.getSkill();
            if (Objects.nonNull(skill)) {
                this.skillCode = skill.getCode();
                this.skillName = skill.getName();
                this.skillCategory = skill.getCategory();
            }
        }
    }
}

