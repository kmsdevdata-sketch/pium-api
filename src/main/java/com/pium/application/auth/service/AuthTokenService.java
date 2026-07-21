package com.pium.application.auth.service;

import com.pium.adapter.outbound.auth.jwt.JwtProperties;
import com.pium.application.auth.dto.AuthTokenView;
import com.pium.application.auth.exception.AuthApplicationErrorCode;
import com.pium.application.auth.exception.AuthApplicationException;
import com.pium.application.auth.required.IssueAccessTokenPort;
import com.pium.application.auth.required.LoadUserPort;
import com.pium.application.auth.required.RefreshTokenStorePort;
import com.pium.domain.auth.model.RefreshToken;
import com.pium.domain.user.model.User;
import com.pium.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthTokenService {

    private static final String BEARER = "Bearer";
    private static final int REFRESH_TOKEN_BYTE_SIZE = 32;

    private final IssueAccessTokenPort issueAccessTokenPort;
    private final RefreshTokenStorePort refreshTokenStorePort;
    private final LoadUserPort loadUserPort;
    private final JwtProperties jwtProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthTokenView issue(UserId userId) {
        String accessToken = issueAccessTokenPort.issue(userId);
        String refreshToken = generateRefreshToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.refreshTokenExpirationSeconds());

        refreshTokenStorePort.save(RefreshToken.create(
                userId,
                hash(refreshToken),
                expiresAt
        ));

        return new AuthTokenView(
                BEARER,
                accessToken,
                refreshToken,
                jwtProperties.accessTokenExpirationSeconds(),
                jwtProperties.refreshTokenExpirationSeconds()
        );
    }

    public AuthTokenView refresh(String refreshToken) {
        RefreshToken storedToken = refreshTokenStorePort
                .findActiveByTokenHash(hash(refreshToken), LocalDateTime.now())
                .orElseThrow(() -> new AuthApplicationException(AuthApplicationErrorCode.INVALID_REFRESH_TOKEN));

        User user = loadUserPort.load(storedToken.getUserId())
                .orElseThrow(() -> new AuthApplicationException(AuthApplicationErrorCode.INVALID_REFRESH_TOKEN));

        storedToken.revoke();
        refreshTokenStorePort.save(storedToken);

        return issue(user.getId());
    }

    public void logout(String refreshToken) {
        refreshTokenStorePort
                .findActiveByTokenHash(hash(refreshToken), LocalDateTime.now())
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenStorePort.save(token);
                });
    }

    public void revokeAll(UserId userId) {
        refreshTokenStorePort.revokeAllByUserId(userId);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTE_SIZE];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthApplicationException(AuthApplicationErrorCode.INVALID_REFRESH_TOKEN);
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", e);
        }
    }
}
