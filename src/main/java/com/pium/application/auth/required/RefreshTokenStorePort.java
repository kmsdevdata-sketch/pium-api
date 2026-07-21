package com.pium.application.auth.required;

import com.pium.domain.auth.model.RefreshToken;
import com.pium.domain.user.vo.UserId;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenStorePort {

    void save(RefreshToken refreshToken);

    Optional<RefreshToken> findActiveByTokenHash(String tokenHash, LocalDateTime now);

    void revokeAllByUserId(UserId userId);
}
