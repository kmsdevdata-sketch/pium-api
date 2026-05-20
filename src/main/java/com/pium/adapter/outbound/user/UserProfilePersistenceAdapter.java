package com.pium.adapter.outbound.user;

import com.pium.adapter.outbound.user.persistence.entity.UserProfileEntity;
import com.pium.adapter.outbound.user.persistence.repository.UserProfileJpaRepository;
import com.pium.application.auth.required.LoadUserProfilePort;
import com.pium.application.auth.required.SaveUserProfilePort;
import com.pium.domain.user.model.UserProfile;
import com.pium.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class UserProfilePersistenceAdapter implements SaveUserProfilePort, LoadUserProfilePort {

    private final UserProfileJpaRepository userProfileJpaRepository;

    @Override
    public void save(UserProfile userProfile) {
        userProfileJpaRepository.save(UserProfileEntity.from(userProfile));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfile> findByUserId(UserId userId) {
        return userProfileJpaRepository.findByUserId(userId.value())
                .map(UserProfileEntity::toDomain);
    }
}
