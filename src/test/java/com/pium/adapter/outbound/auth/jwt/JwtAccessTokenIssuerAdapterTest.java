package com.pium.adapter.outbound.auth.jwt;

import com.pium.domain.user.vo.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAccessTokenIssuerAdapterTest {

    private static final String SECRET = "test-jwt-secret-key-must-be-long-enough-123";

    @Test
    void issue_유저아이디를_subject로_가지는_jwt를_발급한다() {
        JwtProperties jwtProperties = new JwtProperties(SECRET, 1800L);
        JwtAccessTokenIssuerAdapter adapter = new JwtAccessTokenIssuerAdapter(jwtProperties);
        UserId userId = UserId.of("user-001");
        Instant beforeIssue = Instant.now();

        String token = adapter.issue(userId);

        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(userId.value());
        assertThat(claims.getIssuedAt().toInstant()).isAfterOrEqualTo(beforeIssue.minusSeconds(1));
        assertThat(claims.getExpiration().toInstant()).isAfter(claims.getIssuedAt().toInstant());
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
}
