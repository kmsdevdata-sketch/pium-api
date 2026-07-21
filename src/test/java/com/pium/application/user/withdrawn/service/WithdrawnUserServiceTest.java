package com.pium.application.user.withdrawn.service;

import com.pium.application.auth.required.LoadUserPort;
import com.pium.application.auth.required.SaveUserPort;
import com.pium.application.auth.service.AuthTokenService;
import com.pium.domain.user.exception.UserErrorCode;
import com.pium.domain.user.exception.UserException;
import com.pium.domain.user.model.User;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WithdrawnUserServiceTest {

    private final LoadUserPort loadUserPort = mock(LoadUserPort.class);
    private final SaveUserPort saveUserPort = mock(SaveUserPort.class);
    private final AuthTokenService authTokenService = mock(AuthTokenService.class);

    private final WithdrawnUserService service = new WithdrawnUserService(loadUserPort, saveUserPort, authTokenService);

    @Test
    void withdrawn_활성사용자를_탈퇴상태로_저장한다() {
        User user = User.create();
        when(loadUserPort.load(user.getId())).thenReturn(Optional.of(user));

        service.withdrawn(user.getId());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(saveUserPort).save(userCaptor.capture());
        verify(authTokenService).revokeAll(user.getId());

        assertThat(userCaptor.getValue().getStatus().name()).isEqualTo("WITHDRAWN");
    }

    @Test
    void withdrawn_활성사용자를_찾지못하면_예외를_던지고_저장하지_않는다() {
        UserId userId = UserId.of("missing-user");
        when(loadUserPort.load(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.withdrawn(userId))
                .isInstanceOf(UserException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.INVALID_USER_ID);

        verify(saveUserPort, never()).save(org.mockito.ArgumentMatchers.any());
        verify(authTokenService, never()).revokeAll(org.mockito.ArgumentMatchers.any());
    }
}
