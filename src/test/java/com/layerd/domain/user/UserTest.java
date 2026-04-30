package com.layerd.domain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    User user;

    @BeforeEach
    void setup() {
        user = User.create();
    }

    @Test
    void 생성시_기본값_검증() {
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getLastLoginAt()).isNotNull();
    }
    
    @Test
    void 로그인시_lastLoginAt_갱신() {
        Instant before = user.getLastLoginAt();

        user.login();

        assertThat(user.getLastLoginAt()).isAfter(before);
    }

    @Test
    void 탈퇴하면_status전환_deletedAt_갱신() {
        user.deleted();

        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(user.getDeletedAt()).isNotNull();
    }

    @Test
    void 차단하면_status_전환() {
        user.ban();

        assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
    }

    @Test
    void 차단해제하면_ACTIVE_상태로_전환() {
        user.ban();

        user.unban();

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void 탈퇴상태일시_로그인불가() {
        user.deleted();

        assertThatThrownBy(() -> user.login());
    }

    @Test
    void 차단상태일시_로그인불가() {
        user.ban();

        assertThatThrownBy(() -> user.login());
    }

}