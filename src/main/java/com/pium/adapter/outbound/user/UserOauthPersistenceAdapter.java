package com.pium.adapter.outbound.user;

import com.pium.adapter.outbound.user.persistence.entity.UserOauthEntity;
import com.pium.adapter.outbound.user.persistence.repository.UserOauthJpaRepository;
import com.pium.application.auth.required.LoadUserOauthPort;
import com.pium.application.auth.required.SaveUserOauthPort;
import com.pium.domain.user.enumtype.OauthProvider;
import com.pium.domain.user.model.UserOauth;
import com.pium.domain.user.vo.ProviderUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class UserOauthPersistenceAdapter implements SaveUserOauthPort, LoadUserOauthPort {

    private final UserOauthJpaRepository userOauthJpaRepository;

    @Override
    public void save(UserOauth userOauth) {
        userOauthJpaRepository.save(UserOauthEntity.from(userOauth));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserOauth> findByProviderAndProviderUserId(
            OauthProvider provider,
            ProviderUserId providerUserId
    ) {
        return userOauthJpaRepository.findByProviderAndProviderUserId(provider, providerUserId.value())
                .map(UserOauthEntity::toDomain);
    }
}
