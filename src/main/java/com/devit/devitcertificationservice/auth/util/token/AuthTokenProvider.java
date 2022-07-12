package com.devit.devitcertificationservice.auth.util.token;

import com.devit.devitcertificationservice.auth.security.UserPrincial;
import com.devit.devitcertificationservice.exception.ErrorCode;
import com.devit.devitcertificationservice.exception.TokenValidFailedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class AuthTokenProvider {

    private final Key key;

    public AuthTokenProvider(String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public AuthToken createAuthToken(Date expiry) {
        return new AuthToken(expiry, key);
    }

    public AuthToken createAuthToken(String email, String nickName, UUID uid, String role, String type, Date expiry) {
        return new AuthToken(email, nickName, uid, role, type, expiry, key);
    }

    public AuthToken convertAuthToken(String token) {
        return new AuthToken(token, key);
    }

    public Authentication getAuthentication(AuthToken authToken) throws TokenValidFailedException {

        if (authToken.validate()) {

            Claims claims = authToken.getTokenClaims();
            Collection<? extends GrantedAuthority> authorities =
                    Arrays.stream(new String[]{claims.get(AuthToken.AUTHORITIES_KEY).toString()})
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            log.debug("claims subject := [{}]", claims.getSubject());

            UserPrincial userPrincial = new UserPrincial((String) claims.get(AuthToken.USER_ID));
            return new UsernamePasswordAuthenticationToken(userPrincial, authToken, authorities);
        } else {
            throw new TokenValidFailedException(ErrorCode.UNAUTHORIZED, "authToken not validate");
        }
    }

}
