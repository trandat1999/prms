package com.tranhuudat.prms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;

@Table(name = "tbl_role")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Role extends BaseInformation implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return getCode();
    }
}
