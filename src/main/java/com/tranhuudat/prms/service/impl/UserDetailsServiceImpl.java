package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.entity.Role;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author DatNuclear 04/05/2026 04:50 PM
 * @project prms
 * @package com.tranhuudat.prms.service.impl
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService, ApplicationListener<AuthenticationSuccessEvent> {
    private final UserRepository userRepository;
    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String userName = ((UserDetails) event.getAuthentication().
                getPrincipal()).getUsername();
        Optional<User> userOptional = userRepository.findByUsername(userName);
        User user = userOptional.orElseThrow(() -> new UsernameNotFoundException("username not found: " + userName));
        user.setLastLogin(new Date());
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByUsername(username);
        User user = userOptional.orElseThrow(() -> new UsernameNotFoundException("username not found: " + username));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.isEnabled()
                , user.getAccountNonExpired(), user.getCredentialsNonExpired(), user.getAccountNonLocked(), grantedAuthorities( user.getRoles()));
    }

    private Collection<? extends GrantedAuthority> grantedAuthorities(Set<Role> roles) {
        List<SimpleGrantedAuthority> list = new ArrayList<>();
        for (Role role : roles) {
            list.add(new SimpleGrantedAuthority(role.getCode()));
        }
        return list;
    }
}
