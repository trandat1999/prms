package com.tranhuudat.prms.config;

import com.tranhuudat.prms.entity.Role;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.enums.RoleEnum;
import com.tranhuudat.prms.repository.RoleRepository;
import com.tranhuudat.prms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking and initializing default data...");

        // Ensure roles exist
        Role supperAdminRole = roleRepository.findByCode(RoleEnum.SUPPER_ADMIN.getCode())
                .orElseGet(() -> {
                    log.info("Creating role: {}", RoleEnum.SUPPER_ADMIN.getCode());
                    Role role = Role.builder()
                            .code(RoleEnum.SUPPER_ADMIN.getCode())
                            .name(RoleEnum.SUPPER_ADMIN.getName())
                            .description(RoleEnum.SUPPER_ADMIN.getName())
                            .voided(false)
                            .build();
                    return roleRepository.save(role);
                });

        // Ensure other roles exist as well for completeness based on RoleEnum
        for (RoleEnum roleEnum : RoleEnum.values()) {
            if (roleEnum != RoleEnum.SUPPER_ADMIN) {
                roleRepository.findByCode(roleEnum.getCode())
                        .orElseGet(() -> {
                            log.info("Creating role: {}", roleEnum.getCode());
                            Role role = Role.builder()
                                    .code(roleEnum.getCode())
                                    .name(roleEnum.getName())
                                    .description(roleEnum.getName())
                                    .voided(false)
                                    .build();
                            return roleRepository.save(role);
                        });
            }
        }

        // Ensure supper_admin user exists
        final String SUPPER_ADMIN_USERNAME = "supper_admin";
        if (!userRepository.existsByUsername(SUPPER_ADMIN_USERNAME)) {
            log.info("Creating user: {}", SUPPER_ADMIN_USERNAME);
            Set<Role> roles = new HashSet<>();
            roles.add(supperAdminRole);

            User user = User.builder()
                    .username(SUPPER_ADMIN_USERNAME)
                    .password(passwordEncoder.encode("123456"))
                    .email("supperadmin@gmail.com")
                    .fullName("Supper Admin")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(roles)
                    .voided(false)
                    .build();
            userRepository.save(user);
        }

        log.info("Default data initialization completed.");
    }
}
