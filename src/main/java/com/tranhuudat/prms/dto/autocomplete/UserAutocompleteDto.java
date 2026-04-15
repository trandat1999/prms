package com.tranhuudat.prms.dto.autocomplete;

import com.tranhuudat.prms.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAutocompleteDto {
    private UUID id;
    private String username;
    private String email;
    private String fullName;

    public UserAutocompleteDto(User entity) {
        if (Objects.nonNull(entity)) {
            BeanUtils.copyProperties(entity, this);
        }
    }
}

