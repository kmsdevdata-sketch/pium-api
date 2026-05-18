package com.pium.adapter.outbound.user.persistence.entity;

import com.pium.domain.user.enumtype.OauthProvider;
import com.pium.domain.user.model.UserOauth;
import com.pium.domain.user.vo.ProviderUserId;
import com.pium.domain.user.vo.UserId;
import com.pium.domain.user.vo.UserOauthId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_oauth",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_oauth_provider_identity", columnNames = {"provider", "provider_user_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOauthEntity {

    @Id
    @Column(name = "user_oauth_id", nullable = false, length = 64)
    private String userOauthId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 32)
    private OauthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 128)
    private String providerUserId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    private UserOauthEntity(
            String userOauthId,
            String userId,
            OauthProvider provider,
            String providerUserId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime lastLoginAt
    ) {
        this.userOauthId = userOauthId;
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    public static UserOauthEntity from(UserOauth userOauth) {
        return new UserOauthEntity(
                userOauth.getId().value(),
                userOauth.getUserId().value(),
                userOauth.getProvider(),
                userOauth.getProviderUserId().value(),
                userOauth.getCreatedAt(),
                userOauth.getUpdatedAt(),
                userOauth.getLastLoginAt()
        );
    }

    public UserOauth toDomain() {
        return UserOauth.reconstitute(
                UserOauthId.of(userOauthId),
                UserId.of(userId),
                provider,
                ProviderUserId.of(providerUserId),
                createdAt,
                updatedAt,
                lastLoginAt
        );
    }
}
