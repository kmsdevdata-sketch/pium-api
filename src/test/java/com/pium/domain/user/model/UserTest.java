package com.pium.domain.user.model;

import com.pium.domain.user.enumtype.UserRole;
import com.pium.domain.user.enumtype.UserStatus;
import com.pium.domain.user.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


class UserTest {

    User user;

    @BeforeEach
    void setUp() {
        user = UserFixture.createUser();
    }

    @Test
    void 유저_상태_검증() {
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void 유저_권한_검증() {
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void 유저_아이디_검증() {
        assertThat(user.getId()).isNotNull();
    }

    @Test
    void 유저_생성_업데이트_시각_검증() {
        assertThat(user.getCreatedAt()).isEqualTo(user.getUpdatedAt());
    }

    @Test
    void 유저_복원_검증() {
        User reconstituted =
                User.reconstitute(user.getId(), user.getRole(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt());

        assertThat(user.getId()).isEqualTo(reconstituted.getId());
        assertThat(user.getRole()).isEqualTo(reconstituted.getRole());
        assertThat(user.getStatus()).isEqualTo(reconstituted.getStatus());
        assertThat(user.getCreatedAt()).isEqualTo(reconstituted.getCreatedAt());
        assertThat(user.getUpdatedAt()).isEqualTo(reconstituted.getUpdatedAt());
    }

    @Test
    void 유저_탈퇴_검증() {
        LocalDateTime beforeWithdraw = user.getUpdatedAt();
        user.withdraw();

        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(user.getUpdatedAt()).isAfter(beforeWithdraw);

        assertThatThrownBy(() -> user.withdraw());
    }

    @Test
    void 유저_차단_검증() {
        LocalDateTime beforeBan = user.getUpdatedAt();
        user.ban();

        assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
        assertThat(user.getUpdatedAt()).isAfter(beforeBan);

        assertThatThrownBy(() -> user.ban());
    }

    @Test
    void 유저_탈퇴시_활성상태_검증() {
        assertThat(user.isActive()).isTrue();

        user.withdraw();
        assertThat(user.isActive()).isFalse();
    }

    @Test
    void 유저_차단시_활성상태_검증() {
        assertThat(user.isActive()).isTrue();

        user.ban();
        assertThat(user.isActive()).isFalse();
    }
}