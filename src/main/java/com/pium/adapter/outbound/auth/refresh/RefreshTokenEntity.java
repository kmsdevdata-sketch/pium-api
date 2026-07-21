package com.pium.adapter.outbound.auth.refresh;

import com.pium.domain.auth.model.RefreshToken;
import com.pium.domain.user.vo.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenEntity {

    @Id
    @Column(name = "refresh_token_id", nullable = false, length = 64)
    private String refreshTokenId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private RefreshTokenEntity(
            String refreshTokenId,
            String userId,
            String tokenHash,
            LocalDateTime expiresAt,
            LocalDateTime revokedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.refreshTokenId = refreshTokenId;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RefreshTokenEntity from(RefreshToken refreshToken) {
        return new RefreshTokenEntity(
                refreshToken.getId(),
                refreshToken.getUserId().value(),
                refreshToken.getTokenHash(),
                refreshToken.getExpiresAt(),
                refreshToken.getRevokedAt(),
                refreshToken.getCreatedAt(),
                refreshToken.getUpdatedAt()
        );
    }

    public RefreshToken toDomain() {
        return RefreshToken.reconstitute(
                refreshTokenId,
                UserId.of(userId),
                tokenHash,
                expiresAt,
                createdAt,
                updatedAt,
                revokedAt
        );
    }
}
