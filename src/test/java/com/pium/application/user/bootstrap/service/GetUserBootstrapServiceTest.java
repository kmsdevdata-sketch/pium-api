package com.pium.application.user.bootstrap.service;

import com.pium.application.auth.required.LoadUserProfilePort;
import com.pium.application.user.bootstrap.dto.UserBootstrapView;
import com.pium.application.user.bootstrap.required.CheckUserDiagnosisPort;
import com.pium.domain.user.model.UserProfile;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetUserBootstrapServiceTest {

    private final CheckUserDiagnosisPort checkUserDiagnosisPort = mock(CheckUserDiagnosisPort.class);
    private final LoadUserProfilePort loadUserProfilePort = mock(LoadUserProfilePort.class);
    private final GetUserBootstrapService service = new GetUserBootstrapService(checkUserDiagnosisPort,loadUserProfilePort);

    @Test
    void getUserBootstrap_진단이력이_없으면_설문으로_진입한다() {
        UserId userId = UserId.of("user-test-001");
        when(loadUserProfilePort.findByUserId(userId))
                .thenReturn(Optional.of(UserProfile.create(userId, "피움닉네임", null)));
        when(checkUserDiagnosisPort.existsByUserId(userId)).thenReturn(false);

        UserBootstrapView result = service.getUserBootstrap(userId);

        assertThat(result.userName()).isEqualTo("피움닉네임");
        assertThat(result.hasDiagnosis()).isFalse();
        assertThat(result.entryPoint()).isEqualTo(UserBootstrapView.EntryPoint.SURVEY);
        verify(loadUserProfilePort).findByUserId(userId);
        verify(checkUserDiagnosisPort).existsByUserId(userId);
    }

    @Test
    void getUserBootstrap_진단이력이_있으면_Home으로_진입한다() {
        UserId userId = UserId.of("user-test-002");
        when(loadUserProfilePort.findByUserId(userId))
                .thenReturn(Optional.of(UserProfile.create(userId, "피움닉네임", null)));
        when(checkUserDiagnosisPort.existsByUserId(userId)).thenReturn(true);

        UserBootstrapView result = service.getUserBootstrap(userId);

        assertThat(result.userName()).isEqualTo("피움닉네임");
        assertThat(result.hasDiagnosis()).isTrue();
        assertThat(result.entryPoint()).isEqualTo(UserBootstrapView.EntryPoint.HOME);
        verify(loadUserProfilePort).findByUserId(userId);
        verify(checkUserDiagnosisPort).existsByUserId(userId);
    }
}
