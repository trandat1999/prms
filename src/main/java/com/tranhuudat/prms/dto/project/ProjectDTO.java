package com.tranhuudat.prms.dto.project;

import com.tranhuudat.prms.dto.BaseDTO;
import com.tranhuudat.prms.entity.Project;
import com.tranhuudat.prms.enums.PriorityEnum;
import com.tranhuudat.prms.enums.ProjectStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * @author DatNuclear 04/09/2026 04:58 PM
 * @project prms
 * @package com.tranhuudat.prms.dto
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ProjectDTO extends BaseDTO {
    UUID managerId;
    PriorityEnum priority;
    Date startDate;
    Date endDate;
    ProjectStatusEnum status;
    Double progressPercentage;
    String managerName;

    public ProjectDTO(Project entity) {
        if(Objects.nonNull(entity)){
            BeanUtils.copyProperties(entity,this);
            if(Objects.nonNull(entity.getManager())){
                this.managerName = entity.getManager().getFullName();
            }
        }
    }
}
