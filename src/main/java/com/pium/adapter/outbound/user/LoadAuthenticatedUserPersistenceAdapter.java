package com.pium.adapter.outbound.user;

import com.pium.adapter.inbound.web.auth.AuthenticatedUser;
import com.pium.adapter.outbound.user.persistence.entity.UserEntity;
import com.pium.adapter.outbound.user.persistence.repository.UserJpaRepository;
import com.pium.adapter.outbound.user.persistence.repository.UserProfileJpaRepository;
import com.pium.application.auth.required.LoadAuthenticatedUserPort;
import com.pium.domain.user.enumtype.UserStatus;
import com.pium.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoadAuthenticatedUserPersistenceAdapter implements LoadAuthenticatedUserPort {

    private final UserJpaRepository userJpaRepository;
    private final UserProfileJpaRepository userProfileJpaRepository;

    // 단순 시큐리티 principal조립용이여서 도메인 모델로 재매핑 하지않음
    @Override
    public Optional<AuthenticatedUser> load(UserId userId) {
        return userJpaRepository
                .findByUserIdAndStatus(userId.value(), UserStatus.ACTIVE)
                .flatMap(this::toAuthenticatedUser);
    }

    private Optional<AuthenticatedUser> toAuthenticatedUser(UserEntity userEntity) {
        return userProfileJpaRepository.findByUserId(userEntity.getUserId())
                .map(userProfileEntity -> new AuthenticatedUser(
                        userEntity.getUserId(),
                        userProfileEntity.getNickname(),
                        userEntity.getRole()
                ));
    }

}
