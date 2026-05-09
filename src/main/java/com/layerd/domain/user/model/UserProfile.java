package com.layerd.domain.user.model;

import com.layerd.domain.user.exception.UserErrorCode;
import com.layerd.domain.user.exception.UserException;
import com.layerd.domain.user.vo.UserId;
import com.layerd.domain.user.vo.UserProfileId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserProfile {

    private final UserProfileId id;
    private final UserId userId;
    private final LocalDateTime createdAt;

    private String nickname;
    private String profileImageUrl;
    private LocalDateTime updatedAt;

    private UserProfile(
            UserProfileId id,
            UserId userId,
            String nickname,
            String profileImageUrl,
            LocalDateTime createdAt
    ) {
        validateNickname(nickname);

        this.id = id;
        this.userId = userId;
        this.nickname = nickname.trim();
        this.profileImageUrl = normalizeProfileImageUrl(profileImageUrl);
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static UserProfile create(
            UserId userId,
            String nickname,
            String profileImageUrl
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new UserProfile(
                UserProfileId.newId(),
                userId,
                nickname,
                profileImageUrl,
                now
        );
    }

    public static UserProfile reconstitute(
            UserProfileId id,
            UserId userId,
            String nickname,
            String profileImageUrl,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        UserProfile profile = new UserProfile(id, userId, nickname, profileImageUrl, createdAt);
        profile.updatedAt = updatedAt;
        return profile;
    }

    public void changeNickname(String nickname) {
        validateNickname(nickname);
        this.nickname = nickname.trim();
        this.updatedAt = LocalDateTime.now();
    }

    public void changeProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = normalizeProfileImageUrl(profileImageUrl);
        this.updatedAt = LocalDateTime.now();
    }

    private static void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new UserException(UserErrorCode.INVALID_NICKNAME);
        }
    }

    private static String normalizeProfileImageUrl(String profileImageUrl) {
        if (profileImageUrl == null) {
            return null;
        }
        String trimmed = profileImageUrl.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
