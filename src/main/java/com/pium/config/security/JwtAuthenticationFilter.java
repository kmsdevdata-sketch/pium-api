package com.pium.config.security;

import com.pium.adapter.inbound.web.auth.AuthenticatedUser;
import com.pium.adapter.outbound.auth.jwt.JwtProperties;
import com.pium.application.auth.required.LoadAuthenticatedUserPort;
import com.pium.domain.user.vo.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProperties jwtProperties;
    private final LoadAuthenticatedUserPort loadAuthenticatedUserPort;

    public JwtAuthenticationFilter(JwtProperties jwtProperties, LoadAuthenticatedUserPort loadAuthenticatedUserPort) {
        this.jwtProperties = jwtProperties;
        this.loadAuthenticatedUserPort = loadAuthenticatedUserPort;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            resolveAccessToken(request)
                    .flatMap(this::parseUserId)
                    .flatMap(loadAuthenticatedUserPort::load)
                    .map(this::createAuthentication)
                    .ifPresent(authentication -> SecurityContextHolder.getContext().setAuthentication(authentication));
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }


    private Optional<String> resolveAccessToken(HttpServletRequest request) {

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }

        String accessToken = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (accessToken.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(accessToken);
    }

    private Optional<UserId> parseUserId(String accessToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();

            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(UserId.of(subject));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private UsernamePasswordAuthenticationToken createAuthentication(AuthenticatedUser authenticatedUser) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        authenticatedUser.getAuthorities()
                );

        authentication.setDetails(authenticatedUser);
        return authentication;
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

}
