package com.layerd.domain.user;

import com.layerd.domain.BaseEntity;
import lombok.Getter;

import java.time.Instant;

@Getter
public class User extends BaseEntity {

    private Long id;

    private UserRole role;

    private UserStatus status;

    private Instant lastLoginAt;

    private Instant deletedAt;

    public static User create(){
        User user = new User();
        user.role = UserRole.USER;
        user.status = UserStatus.ACTIVE;
        user.login();
        return user;
    }

    public void login() {
        if (this.status == UserStatus.DELETED || this.status == UserStatus.BANNED) {
            throw new IllegalStateException("로그인 불가 상태");
        }
        this.lastLoginAt = Instant.now();
    }

    public void deleted() {
        this.status = UserStatus.DELETED;
        this.deletedAt = Instant.now();
    }

    public void ban() {
        this.status = UserStatus.BANNED;
    }

    public void unban() {
        this.status = UserStatus.ACTIVE;
    }
}
