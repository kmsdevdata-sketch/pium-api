package com.layerd.domain.user;

import com.layerd.domain.user.exception.UserException;
import com.layerd.domain.user.fixture.UserFixture;
import com.layerd.domain.user.fixture.UserProfileFixture;
import com.layerd.domain.user.model.User;
import com.layerd.domain.user.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserProfileTest {

    User user;
    UserProfile userProfile;

    @BeforeEach
    void setUp() {
        user = UserFixture.createUser();
        userProfile = UserProfileFixture.createUserProfile(user);
    }

    @Test
    void 유저프로필_생성_검증() {
        assertThat(userProfile.getId()).isNotNull();
        assertThat(userProfile.getUserId()).isEqualTo(user.getId());
        assertThat(userProfile.getNickname()).isEqualTo("testNickName");
        assertThat(userProfile.getProfileImageUrl()).isEqualTo("https://test.com/test.png");
        assertThat(userProfile.getCreatedAt()).isEqualTo(userProfile.getUpdatedAt());
    }

    @Test
    void 유저프로필_복원_검증() {
        UserProfile reconstituted = UserProfile.reconstitute(
                userProfile.getId(),
                userProfile.getUserId(),
                userProfile.getNickname(),
                userProfile.getProfileImageUrl(),
                userProfile.getCreatedAt(),
                userProfile.getUpdatedAt()
        );

        assertThat(reconstituted.getId()).isEqualTo(userProfile.getId());
        assertThat(reconstituted.getUserId()).isEqualTo(userProfile.getUserId());
        assertThat(reconstituted.getNickname()).isEqualTo(userProfile.getNickname());
        assertThat(reconstituted.getProfileImageUrl()).isEqualTo(userProfile.getProfileImageUrl());
        assertThat(reconstituted.getCreatedAt()).isEqualTo(userProfile.getCreatedAt());
        assertThat(reconstituted.getUpdatedAt()).isEqualTo(userProfile.getUpdatedAt());
    }

    @Test
    void 유저프로필_닉네임변경_검증() {
        LocalDateTime before = userProfile.getUpdatedAt();

        userProfile.changeNickname("new-nick");

        assertThat(userProfile.getNickname()).isEqualTo("new-nick");
        assertThat(userProfile.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void 유저프로필_프로필이미지변경_검증() {
        LocalDateTime before = userProfile.getUpdatedAt();

        userProfile.changeProfileImageUrl("https://img.layerd/new.png");

        assertThat(userProfile.getProfileImageUrl()).isEqualTo("https://img.layerd/new.png");
        assertThat(userProfile.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void 유저프로필_닉네임_예외_검증() {
        assertThatThrownBy(() -> UserProfile.create(user.getId(), " ", null))
                .isInstanceOf(UserException.class);

        assertThatThrownBy(() -> userProfile.changeNickname(""))
                .isInstanceOf(UserException.class);
    }
}
