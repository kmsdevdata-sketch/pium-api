package com.pium.domain.user.model;

import com.pium.domain.user.enumtype.OauthProvider;
import com.pium.domain.user.fixture.UserFixture;
import com.pium.domain.user.fixture.UserOauthFixture;
import com.pium.domain.user.vo.ProviderUserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserOauthTest {

    UserOauth userOauth;

    @BeforeEach
    void setUp() {
        User user = UserFixture.createUser();
        userOauth = UserOauthFixture.createUserOauth(user);
    }

    @Test
    void 유저어스_생성_검증() {
        assertThat(userOauth.getCreatedAt()).isNotNull();
        assertThat(userOauth.getCreatedAt()).isEqualTo(userOauth.getUpdatedAt());
        assertThat(userOauth.getLastLoginAt()).isNull();
    }

    @Test
    void 유저어스_복원_검증() {
        UserOauth reconstituted =
                UserOauth.reconstitute(userOauth.getId(), userOauth.getUserId(), userOauth.getProvider(), userOauth.getProviderUserId(), userOauth.getCreatedAt(), userOauth.getUpdatedAt(), userOauth.getLastLoginAt());

        assertThat(reconstituted.getId()).isEqualTo(userOauth.getId());
        assertThat(reconstituted.getUserId()).isEqualTo(userOauth.getUserId());
        assertThat(reconstituted.getProvider()).isEqualTo(userOauth.getProvider());
        assertThat(reconstituted.getProviderUserId()).isEqualTo(userOauth.getProviderUserId());
        assertThat(reconstituted.getCreatedAt()).isEqualTo(userOauth.getCreatedAt());
        assertThat(reconstituted.getUpdatedAt()).isEqualTo(userOauth.getUpdatedAt());
        assertThat(reconstituted.getLastLoginAt()).isEqualTo(userOauth.getLastLoginAt());
    }

    @Test
    void 유저어스_로그인기록_검증() {
        LocalDateTime beforeRecordLogin = userOauth.getUpdatedAt();
        assertThat(userOauth.getLastLoginAt()).isNull();

        userOauth.recordLogin();

        assertThat(userOauth.getLastLoginAt()).isNotNull();
        assertThat(userOauth.getUpdatedAt()).isAfter(beforeRecordLogin);
    }

    @Test
    void 유저어스_동일소셜계정_true_검증() {
        boolean result = userOauth.isSameIdentity(
                userOauth.getProvider(),
                userOauth.getProviderUserId()
        );

        assertThat(result).isTrue();
    }

    @Test
    void 유저어스_동일소셜계정_false_검증() {
        boolean differentProvider = userOauth.isSameIdentity(
                OauthProvider.KAKAO,
                userOauth.getProviderUserId()
        );

        boolean differentProviderUserId = userOauth.isSameIdentity(
                userOauth.getProvider(),
                ProviderUserId.of("different-id")
        );

        assertThat(differentProvider).isFalse();
        assertThat(differentProviderUserId).isFalse();
    }

}