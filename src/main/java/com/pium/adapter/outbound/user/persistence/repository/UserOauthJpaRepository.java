package com.pium.adapter.outbound.user.persistence.repository;

import com.pium.adapter.outbound.user.persistence.entity.UserOauthEntity;
import com.pium.domain.user.enumtype.OauthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOauthJpaRepository extends JpaRepository<UserOauthEntity, String> {

    Optional<UserOauthEntity> findByProviderAndProviderUserId(
            OauthProvider provider,
            String providerUserId
    );
}
