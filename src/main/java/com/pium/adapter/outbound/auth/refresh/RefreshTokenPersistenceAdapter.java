package com.pium.adapter.outbound.auth.refresh;

import com.pium.application.auth.required.RefreshTokenStorePort;
import com.pium.domain.auth.model.RefreshToken;
import com.pium.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements RefreshTokenStorePort {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Override
    public void save(RefreshToken refreshToken) {
        refreshTokenJpaRepository.save(RefreshTokenEntity.from(refreshToken));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findActiveByTokenHash(String tokenHash, LocalDateTime now) {
        return refreshTokenJpaRepository
                .findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(tokenHash, now)
                .map(RefreshTokenEntity::toDomain);
    }

    @Override
    public void revokeAllByUserId(UserId userId) {
        refreshTokenJpaRepository.findAllByUserIdAndRevokedAtIsNull(userId.value())
                .stream()
                .map(RefreshTokenEntity::toDomain)
                .peek(RefreshToken::revoke)
                .map(RefreshTokenEntity::from)
                .forEach(refreshTokenJpaRepository::save);
    }
}
