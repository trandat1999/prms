package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.user.UserDto;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author DatNuclear 04/05/2026 09:03 PM
 * @project prms
 * @package com.tranhuudat.prms.service.impl
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseService implements UserService {
    private final UserRepository userRepository;
    @Override
    public BaseResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String username = authentication.getName();
            Optional<User> userOptional = userRepository.findByUsername(username);
            if(userOptional.isEmpty()){
                return getResponse400(null);
            }
            return getResponse200(new UserDto(userOptional.get()));
        }
        return getResponse400(null);
    }
}
