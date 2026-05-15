package com.pium.adapter.outbound.user;

import com.pium.adapter.outbound.user.persistence.entity.UserEntity;
import com.pium.adapter.outbound.user.persistence.repository.UserJpaRepository;
import com.pium.application.auth.required.SaveUserPort;
import com.pium.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class UserPersistenceAdapter implements SaveUserPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public void save(User user) {
        userJpaRepository.save(UserEntity.from(user));
    }
}
