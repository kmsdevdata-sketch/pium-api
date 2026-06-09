package com.pium.adapter.outbound.user;

import com.pium.adapter.outbound.user.persistence.entity.UserEntity;
import com.pium.adapter.outbound.user.persistence.repository.UserJpaRepository;
import com.pium.application.auth.required.LoadUserPort;
import com.pium.application.auth.required.SaveUserPort;
import com.pium.domain.user.enumtype.UserStatus;
import com.pium.domain.user.model.User;
import com.pium.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class UserPersistenceAdapter implements SaveUserPort , LoadUserPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public void save(User user) {
        userJpaRepository.save(UserEntity.from(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> load(UserId userId) {
        return userJpaRepository
                .findByUserIdAndStatus(userId.value(), UserStatus.ACTIVE)
                .map(UserEntity::toDomain);
    }
}
