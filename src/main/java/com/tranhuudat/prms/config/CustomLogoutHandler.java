package com.tranhuudat.prms.config;

import com.tranhuudat.prms.repository.RefreshTokenRepository;
import com.tranhuudat.prms.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.tranhuudat.prms.util.ConstUtil.AUTHORIZATION;
import static com.tranhuudat.prms.util.ConstUtil.BEARER;

/**
 * @author DatNuclear 04/05/2026 04:55 PM
 * @project prms
 * @package com.tranhuudat.prms.config
 */
@Service
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    private final TokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader(AUTHORIZATION);
        if (authHeader == null ||!authHeader.startsWith(BEARER)) {
            return;
        }
        final String jwt = authHeader.substring(BEARER.length());

        var storedToken = tokenRepository.findByToken(jwt)
                .orElse(null);
        if (storedToken != null) {
            var refreshTokens = refreshTokenRepository.findAllByUsernameAndRevokedFalse(storedToken.getUsername());
            if(!CollectionUtils.isEmpty(refreshTokens)){
                refreshTokens.forEach(refreshToken -> refreshToken.setRevoked(true));
                refreshTokenRepository.saveAll(refreshTokens);
            }
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            SecurityContextHolder.clearContext();
        }
    }
}
