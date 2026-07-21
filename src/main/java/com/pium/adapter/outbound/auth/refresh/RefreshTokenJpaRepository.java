package com.pium.adapter.outbound.auth.refresh;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
            String tokenHash,
            LocalDateTime now
    );

    List<RefreshTokenEntity> findAllByUserIdAndRevokedAtIsNull(String userId);
}
