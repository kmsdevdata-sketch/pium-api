package com.layerd.domain.user.model;

import com.layerd.domain.user.enumtype.UserRole;
import com.layerd.domain.user.enumtype.UserStatus;
import com.layerd.domain.user.exception.UserErrorCode;
import com.layerd.domain.user.exception.UserException;
import com.layerd.domain.user.vo.UserId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class User {

    private final UserId id;
    private final LocalDateTime createdAt;

    private UserRole role;
    private UserStatus status;
    private LocalDateTime updatedAt;

    private User(
            UserId id,
            UserRole role,
            UserStatus status,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static User create() {
        LocalDateTime now = LocalDateTime.now();

        return new User(
                UserId.newId(),
                UserRole.USER,
                UserStatus.ACTIVE,
                now
        );
    }

    public static User reconstitute(
            UserId id,
            UserRole role,
            UserStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        User user = new User(id, role, status, createdAt);
        user.updatedAt = updatedAt;
        return user;
    }

    public void withdraw() {
        if (this.status == UserStatus.WITHDRAWN) {
            throw new UserException(UserErrorCode.ALREADY_WITHDRAWN_USER);
        }
        this.status = UserStatus.WITHDRAWN;
        this.updatedAt = LocalDateTime.now();
    }

    public void ban() {
        if (this.status == UserStatus.BANNED) {
            throw new UserException(UserErrorCode.ALREADY_BANNED_USER);
        }

        this.status = UserStatus.BANNED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}
