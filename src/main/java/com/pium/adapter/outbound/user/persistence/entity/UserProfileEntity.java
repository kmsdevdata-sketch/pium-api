package com.pium.adapter.outbound.user.persistence.entity;

import com.pium.domain.user.model.UserProfile;
import com.pium.domain.user.vo.UserId;
import com.pium.domain.user.vo.UserProfileId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_profile",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_profile_user_id", columnNames = {"user_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileEntity {

    @Id
    @Column(name = "user_profile_id", nullable = false, length = 64)
    private String userProfileId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "profile_image_url", length = 512)
    private String profileImageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private UserProfileEntity(
            String userProfileId,
            String userId,
            String nickname,
            String profileImageUrl,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.userProfileId = userProfileId;
        this.userId = userId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserProfileEntity from(UserProfile userProfile) {
        return new UserProfileEntity(
                userProfile.getId().value(),
                userProfile.getUserId().value(),
                userProfile.getNickname(),
                userProfile.getProfileImageUrl(),
                userProfile.getCreatedAt(),
                userProfile.getUpdatedAt()
        );
    }

    public UserProfile toDomain() {
        return UserProfile.reconstitute(
                UserProfileId.of(userProfileId),
                UserId.of(userId),
                nickname,
                profileImageUrl,
                createdAt,
                updatedAt
        );
    }
}
