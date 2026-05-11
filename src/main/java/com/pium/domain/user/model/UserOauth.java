package com.pium.domain.user.model;

import com.pium.domain.user.enumtype.OauthProvider;
import com.pium.domain.user.vo.ProviderUserId;
import com.pium.domain.user.vo.UserId;
import com.pium.domain.user.vo.UserOauthId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserOauth {

    private final UserOauthId id;
    private final UserId userId;
    private final OauthProvider provider;
    private final ProviderUserId providerUserId;
    private final LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    private UserOauth(
            UserOauthId id,
            UserId userId,
            OauthProvider provider,
            ProviderUserId providerUserId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static UserOauth create(
            UserId userId,
            OauthProvider provider,
            ProviderUserId providerUserId
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new UserOauth(
                UserOauthId.newId(),
                userId,
                provider,
                providerUserId,
                now
        );
    }

    public static UserOauth reconstitute(
            UserOauthId id,
            UserId userId,
            OauthProvider provider,
            ProviderUserId providerUserId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime lastLoginAt
    ) {
        UserOauth userOauth = new UserOauth(id, userId, provider, providerUserId, createdAt);
        userOauth.updatedAt = updatedAt;
        userOauth.lastLoginAt = lastLoginAt;
        return userOauth;
    }

    public void recordLogin() {
        LocalDateTime now = LocalDateTime.now();
        this.lastLoginAt = now;
        this.updatedAt = now;
    }

    public boolean isSameIdentity(OauthProvider provider, ProviderUserId providerUserId) {
        return this.provider == provider && this.providerUserId.equals(providerUserId);
    }
}
