package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.config.AppProperties;
import com.tranhuudat.prms.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;

/**
 * @author DatNuclear 04/05/2026 04:05 PM
 * @project prms
 * @package com.tranhuudat.prms.service.impl
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final AppProperties appProperties;

    @Override
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username, appProperties.getExpirationAccessToken());
    }

    @Override
    public String generateToken(Map<String, Object> extractClaims, String username, long expiration) {
        return Jwts.builder()
                .claims(extractClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    @Override
    public boolean isValidToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaimsFromToken(String token) {
        return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] signature = Decoders.BASE64.decode(this.appProperties.getJwtSecret());
        return Keys.hmacShaKeyFor(signature);
    }
}
