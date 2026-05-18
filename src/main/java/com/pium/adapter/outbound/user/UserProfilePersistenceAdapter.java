package com.pium.adapter.outbound.user;

import com.pium.adapter.outbound.user.persistence.entity.UserProfileEntity;
import com.pium.adapter.outbound.user.persistence.repository.UserProfileJpaRepository;
import com.pium.application.auth.required.SaveUserProfilePort;
import com.pium.domain.user.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class UserProfilePersistenceAdapter implements SaveUserProfilePort {

    private final UserProfileJpaRepository userProfileJpaRepository;

    @Override
    public void save(UserProfile userProfile) {
        userProfileJpaRepository.save(UserProfileEntity.from(userProfile));
    }
}
