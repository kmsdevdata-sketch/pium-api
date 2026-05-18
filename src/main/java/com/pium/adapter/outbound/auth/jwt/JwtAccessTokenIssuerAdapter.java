package com.pium.adapter.outbound.auth.jwt;

import com.pium.application.auth.required.IssueAccessTokenPort;
import com.pium.domain.user.vo.UserId;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtAccessTokenIssuerAdapter implements IssueAccessTokenPort {

    private final JwtProperties jwtProperties;

    @Override
    public String issue(UserId userId) {

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.accessTokenExpirationSeconds());


        return Jwts.builder() // JWT생성을 위한 빌더 객체
                .subject(userId.value())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
    }

    private SecretKey signingKey() {
        // HMAC SHA 알고리즘용 SecretKey 객체 생성
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
