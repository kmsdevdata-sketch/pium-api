package com.pium.config.security;

import com.pium.adapter.inbound.web.auth.AuthenticatedUser;
import com.pium.adapter.outbound.auth.jwt.JwtProperties;
import com.pium.application.auth.fixture.AuthFixture;
import com.pium.application.auth.required.LoadAuthenticatedUserPort;
import com.pium.domain.user.vo.UserId;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-jwt-secret-key-must-be-long-enough-123";

    private final LoadAuthenticatedUserPort loadAuthenticatedUserPort = mock(LoadAuthenticatedUserPort.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
            new JwtProperties(SECRET, 3600L),
            loadAuthenticatedUserPort
    );

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_유효한토큰이면_authenticatedUser를_securityContext에_저장한다() throws Exception {
        UserId userId = UserId.of("user-001");
        AuthenticatedUser authenticatedUser = AuthFixture.createAuthenticatedUser(userId);

        when(loadAuthenticatedUserPort.load(userId)).thenReturn(Optional.of(authenticatedUser));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + createAccessToken(userId.value()));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(authenticatedUser);
        assertThat(authentication.getDetails()).isEqualTo(authenticatedUser);
        verify(loadAuthenticatedUserPort).load(userId);
    }

    @Test
    void doFilterInternal_Authorization헤더가_없으면_인증없이_다음필터로_진행한다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(loadAuthenticatedUserPort);
    }

    @Test
    void doFilterInternal_유저조회에_실패하면_인증을_저장하지않는다() throws Exception {
        UserId userId = UserId.of("user-002");
        when(loadAuthenticatedUserPort.load(userId)).thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + createAccessToken(userId.value()));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(loadAuthenticatedUserPort).load(userId);
    }

    private String createAccessToken(String subject) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(3600L);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
}
