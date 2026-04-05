package com.tranhuudat.prms.dto.user;

import com.tranhuudat.prms.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author DatNuclear 04/05/2026 09:06 PM
 * @project prms
 * @package com.tranhuudat.prms.dto.user
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private List<String> roles;

    public UserDto(User entity) {
        if(Objects.nonNull(entity)){
            BeanUtils.copyProperties(entity,this);
        }
    }

}
