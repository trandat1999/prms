package com.tranhuudat.prms.dto.user;

import com.tranhuudat.prms.entity.Role;
import com.tranhuudat.prms.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDetailDto {
    UUID id;
    String username;
    String email;
    String fullName;
    Boolean enabled;
    Boolean accountNonExpired;
    Boolean accountNonLocked;
    Boolean credentialsNonExpired;
    Date lastLogin;
    List<String> roles;

    public UserDetailDto(User entity) {
        if (Objects.nonNull(entity)) {
            BeanUtils.copyProperties(entity, this, "roles");
            if (!CollectionUtils.isEmpty(entity.getRoles())) {
                this.roles = entity.getRoles().stream()
                        .map(Role::getCode)
                        .filter(s -> s != null && !s.isBlank())
                        .sorted()
                        .collect(Collectors.toList());
            }
        }
    }
}

