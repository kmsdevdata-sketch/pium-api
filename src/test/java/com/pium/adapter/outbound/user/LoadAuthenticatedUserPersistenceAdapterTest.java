package com.pium.adapter.outbound.user;

import com.pium.adapter.inbound.web.auth.AuthenticatedUser;
import com.pium.adapter.outbound.user.persistence.entity.UserEntity;
import com.pium.adapter.outbound.user.persistence.entity.UserProfileEntity;
import com.pium.adapter.outbound.user.persistence.repository.UserJpaRepository;
import com.pium.adapter.outbound.user.persistence.repository.UserProfileJpaRepository;
import com.pium.domain.user.model.User;
import com.pium.domain.user.model.UserProfile;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LoadAuthenticatedUserPersistenceAdapterTest {

    private final UserJpaRepository userJpaRepository = mock(UserJpaRepository.class);
    private final UserProfileJpaRepository userProfileJpaRepository = mock(UserProfileJpaRepository.class);

    private final LoadAuthenticatedUserPersistenceAdapter adapter =
            new LoadAuthenticatedUserPersistenceAdapter(userJpaRepository, userProfileJpaRepository);

    @Test
    void load_활성사용자와_프로필이_있으면_authenticatedUser를_반환한다() {
        User user = User.create();
        UserProfile userProfile = UserProfile.create(user.getId(), "피움닉네임", null);

        when(userJpaRepository.findById(user.getId().value())).thenReturn(Optional.of(UserEntity.from(user)));
        when(userProfileJpaRepository.findByUserId(user.getId().value())).thenReturn(Optional.of(UserProfileEntity.from(userProfile)));

        Optional<AuthenticatedUser> result = adapter.load(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo(user.getId().value());
        assertThat(result.get().nickname()).isEqualTo("피움닉네임");
        verify(userProfileJpaRepository).findByUserId(user.getId().value());
    }

    @Test
    void load_비활성사용자는_프로필조회없이_반환하지않는다() {
        User user = User.create();
        user.withdraw();

        when(userJpaRepository.findById(user.getId().value())).thenReturn(Optional.of(UserEntity.from(user)));

        Optional<AuthenticatedUser> result = adapter.load(user.getId());

        assertThat(result).isEmpty();
        verifyNoInteractions(userProfileJpaRepository);
    }

    @Test
    void load_존재하지않는_사용자는_반환하지않는다() {
        UserId missingUserId = UserId.of("missing-user");
        when(userJpaRepository.findById(missingUserId.value())).thenReturn(Optional.empty());

        Optional<AuthenticatedUser> result = adapter.load(missingUserId);

        assertThat(result).isEmpty();
        verifyNoInteractions(userProfileJpaRepository);
    }
}
