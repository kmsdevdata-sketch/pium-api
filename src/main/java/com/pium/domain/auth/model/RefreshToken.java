package com.pium.domain.auth.model;

import com.pium.domain.user.vo.UserId;

import java.time.LocalDateTime;
import java.util.UUID;

public class RefreshToken {

    private final String id;
    private final UserId userId;
    private final String tokenHash;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    private LocalDateTime revokedAt;
    private LocalDateTime updatedAt;

    private RefreshToken(
            String id,
            UserId userId,
            String tokenHash,
            LocalDateTime expiresAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime revokedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.revokedAt = revokedAt;
    }

    public static RefreshToken create(UserId userId, String tokenHash, LocalDateTime expiresAt) {
        LocalDateTime now = LocalDateTime.now();
        return new RefreshToken(
                UUID.randomUUID().toString(),
                userId,
                tokenHash,
                expiresAt,
                now,
                now,
                null
        );
    }

    public static RefreshToken reconstitute(
            String id,
            UserId userId,
            String tokenHash,
            LocalDateTime expiresAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime revokedAt
    ) {
        return new RefreshToken(id, userId, tokenHash, expiresAt, createdAt, updatedAt, revokedAt);
    }

    public void revoke() {
        if (revokedAt != null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        this.revokedAt = now;
        this.updatedAt = now;
    }

    public String getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }
}
