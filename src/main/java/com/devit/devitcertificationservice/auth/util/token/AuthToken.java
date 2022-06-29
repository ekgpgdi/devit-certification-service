package com.devit.devitcertificationservice.auth.util.token;

import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class AuthToken {
    public static final String REFRESH_TOKEN = "refresh_token";
    @Getter
    private final String token;
    private final Key key;

    public static final String AUTHORITIES_KEY = "role";
    public static final String USER_ID = "email";
    public static final String UID = "uid";

    AuthToken(Date expiry, Key key) {
        this.key = key;
        this.token = createAuthToken(expiry);
    }

    AuthToken(String email, UUID uid, String role, Date expiry, Key key) {
        this.key = key;
        this.token = createAuthToken(email, uid, role, expiry);
    }

    private String createAuthToken(Date expiry) {
        return Jwts.builder()
                .setSubject("user")
                .setIssuedAt(new Date())
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String createAuthToken(String email, UUID uid, String role, Date expiry) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject("user")
                .setIssuedAt(now)
                .claim(AUTHORITIES_KEY, role)
                .claim(UID, uid)
                .claim(USER_ID, email)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(expiry)
                .compact();
    }

    public boolean validate() {
        return this.getTokenClaims() != null;
    }

    public Claims getTokenClaims() {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SecurityException e) {
            log.info("Invalid JWT signature.");
        } catch (MalformedJwtException e) {
            log.info("Invalid JWT token.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token.");
        } catch (IllegalArgumentException e) {
            log.info("JWT token compact of handler are invalid.");
        }
        return null;
    }

    public Claims getExpiredTokenClaims() {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token.");
            return e.getClaims();
        }
        return null;
    }
}
