package com.datn.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final SecretKey signingKey;

    private final long accessTokenExpirationMs;

    private final long refreshTokenExpirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secretBase64,
            @Value("${jwt.access-token-expiration-ms}")
            long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms}")
            long refreshTokenExpirationMs
    ) {

        this.signingKey =
                Keys.hmacShaKeyFor(
                        java.util.Base64
                                .getDecoder()
                                .decode(secretBase64)
                );

        this.accessTokenExpirationMs =
                accessTokenExpirationMs;

        this.refreshTokenExpirationMs =
                refreshTokenExpirationMs;
    }

    public String generateAccessToken(
            CustomUserDetails userDetails
    ) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("uid", userDetails.getUserId());
        claims.put("role", userDetails.getRoleCode());
        claims.put("type", "ACCESS");

        return buildToken(
                claims,
                userDetails.getUsername(),
                accessTokenExpirationMs
        );
    }

    public String generateRefreshToken(
            CustomUserDetails userDetails
    ) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("uid", userDetails.getUserId());
        claims.put("type", "REFRESH");

        return buildToken(
                claims,
                userDetails.getUsername(),
                refreshTokenExpirationMs
        );
    }

    private String buildToken(
            Map<String, Object> claims,
            String subject,
            long expirationMs
    ) {

        Date now = new Date();

        Date expiry =
                new Date(
                        now.getTime() + expirationMs
                );

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    public Long extractUserId(String token) {

        return extractAllClaims(token)
                .get("uid", Long.class);
    }

    public String extractTokenType(String token) {

        return extractAllClaims(token)
                .get("type", String.class);
    }

    public String extractRole(String token) {

        return extractAllClaims(token)
                .get("role", String.class);
    }

    public boolean isTokenValid(
            String token,
            String expectedEmail
    ) {

        final String email =
                extractEmail(token);

        return email.equals(expectedEmail)
                && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {

        try {

            return extractClaim(
                    token,
                    Claims::getExpiration
            ).before(new Date());

        } catch (ExpiredJwtException e) {

            return true;
        }
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        final Claims claims =
                extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}