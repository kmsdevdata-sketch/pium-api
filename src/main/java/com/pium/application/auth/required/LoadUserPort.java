package com.pium.application.auth.required;

import com.pium.domain.user.model.User;
import com.pium.domain.user.vo.UserId;

import java.util.Optional;

/**
 * 유저 읽기 포트
 */
public interface LoadUserPort {
    Optional<User> load(UserId userId);
}
