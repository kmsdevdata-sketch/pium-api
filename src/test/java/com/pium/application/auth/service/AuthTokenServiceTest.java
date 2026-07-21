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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthTokenServiceTest {

    private static final JwtProperties JWT_PROPERTIES = new JwtProperties(
            "test-jwt-secret-key-must-be-long-enough-123",
            3600L,
            1209600L
    );

    private final IssueAccessTokenPort issueAccessTokenPort = mock(IssueAccessTokenPort.class);
    private final RefreshTokenStorePort refreshTokenStorePort = mock(RefreshTokenStorePort.class);
    private final LoadUserPort loadUserPort = mock(LoadUserPort.class);

    private final AuthTokenService service = new AuthTokenService(
            issueAccessTokenPort,
            refreshTokenStorePort,
            loadUserPort,
            JWT_PROPERTIES
    );

    @Test
    void issue_accessToken과_refreshToken을_발급하고_refreshTokenHash를_저장한다() {
        UserId userId = UserId.of("user-001");
        when(issueAccessTokenPort.issue(userId)).thenReturn("access-token");

        AuthTokenView view = service.issue(userId);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenStorePort).save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertThat(view.tokenType()).isEqualTo("Bearer");
        assertThat(view.accessToken()).isEqualTo("access-token");
        assertThat(view.refreshToken()).isNotBlank();
        assertThat(view.accessTokenExpiresInSeconds()).isEqualTo(3600L);
        assertThat(view.refreshTokenExpiresInSeconds()).isEqualTo(1209600L);
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getTokenHash()).isEqualTo(hash(view.refreshToken()));
        assertThat(saved.getTokenHash()).isNotEqualTo(view.refreshToken());
        assertThat(saved.getRevokedAt()).isNull();
    }

    @Test
    void refresh_기존_refreshToken을_폐기하고_새토큰묶음을_발급한다() {
        User user = User.create();
        String oldRefreshToken = "old-refresh-token";
        RefreshToken storedToken = RefreshToken.create(
                user.getId(),
                hash(oldRefreshToken),
                LocalDateTime.now().plusDays(7)
        );

        when(refreshTokenStorePort.findActiveByTokenHash(eq(hash(oldRefreshToken)), any(LocalDateTime.class)))
                .thenReturn(Optional.of(storedToken));
        when(loadUserPort.load(user.getId())).thenReturn(Optional.of(user));
        when(issueAccessTokenPort.issue(user.getId())).thenReturn("new-access-token");

        AuthTokenView view = service.refresh(oldRefreshToken);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenStorePort, org.mockito.Mockito.times(2)).save(captor.capture());

        List<RefreshToken> savedTokens = captor.getAllValues();
        assertThat(view.accessToken()).isEqualTo("new-access-token");
        assertThat(savedTokens.get(0).getId()).isEqualTo(storedToken.getId());
        assertThat(savedTokens.get(0).getRevokedAt()).isNotNull();
        assertThat(savedTokens.get(1).getId()).isNotEqualTo(storedToken.getId());
        assertThat(savedTokens.get(1).getRevokedAt()).isNull();
    }

    @Test
    void refresh_유효한_refreshToken이_없으면_예외를_던진다() {
        when(refreshTokenStorePort.findActiveByTokenHash(eq(hash("missing-refresh-token")), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh("missing-refresh-token"))
                .isInstanceOf(AuthApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthApplicationErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void logout_활성_refreshToken을_폐기한다() {
        UserId userId = UserId.of("user-001");
        String refreshToken = "refresh-token";
        RefreshToken storedToken = RefreshToken.create(
                userId,
                hash(refreshToken),
                LocalDateTime.now().plusDays(7)
        );
        when(refreshTokenStorePort.findActiveByTokenHash(eq(hash(refreshToken)), any(LocalDateTime.class)))
                .thenReturn(Optional.of(storedToken));

        service.logout(refreshToken);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenStorePort).save(captor.capture());
        assertThat(captor.getValue().getRevokedAt()).isNotNull();
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
