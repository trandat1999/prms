package com.tranhuudat.prms.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * @author DatNuclear 04/05/2026 04:03 PM
 * @project prms
 * @package com.tranhuudat.prms.service
 */
public interface JwtService {
    String getUsernameFromToken(String token);
    String generateToken(String username);
    String generateToken(Map<String, Object> extractClaims, String username, long expiration);
    boolean isValidToken(String token, UserDetails userDetails);
    boolean isTokenExpired(String token);
    Date extractExpiration(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
}
