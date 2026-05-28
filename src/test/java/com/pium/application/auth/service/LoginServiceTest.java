package com.pium.application.auth.service;

import com.pium.application.auth.dto.AuthTokenView;
import com.pium.application.auth.dto.LoginCommand;
import com.pium.application.auth.fixture.AuthFixture;
import com.pium.application.auth.required.ExchangeGoogleTokenPort;
import com.pium.application.auth.required.ExchangeTossTokenPort;
import com.pium.application.auth.required.IssueAccessTokenPort;
import com.pium.application.auth.required.LoadGoogleUserPort;
import com.pium.application.auth.required.LoadTossUserPort;
import com.pium.application.auth.required.LoadUserOauthPort;
import com.pium.application.auth.required.SaveUserOauthPort;
import com.pium.application.auth.required.SaveUserPort;
import com.pium.application.auth.required.SaveUserProfilePort;
import com.pium.application.auth.required.dto.GoogleAccessToken;
import com.pium.application.auth.required.dto.GoogleAuthenticatedUser;
import com.pium.application.auth.required.dto.TossAccessToken;
import com.pium.application.auth.required.dto.TossAuthenticatedUser;
import com.pium.domain.user.enumtype.OauthProvider;
import com.pium.domain.user.model.User;
import com.pium.domain.user.model.UserOauth;
import com.pium.domain.user.model.UserProfile;
import com.pium.domain.user.vo.ProviderUserId;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginServiceTest {

    private final ExchangeTossTokenPort exchangeTossTokenPort = mock(ExchangeTossTokenPort.class);
    private final LoadTossUserPort loadTossUserPort = mock(LoadTossUserPort.class);
    private final ExchangeGoogleTokenPort exchangeGoogleTokenPort = mock(ExchangeGoogleTokenPort.class);
    private final LoadGoogleUserPort loadGoogleUserPort = mock(LoadGoogleUserPort.class);
    private final LoadUserOauthPort loadUserOauthPort = mock(LoadUserOauthPort.class);
    private final SaveUserPort saveUserPort = mock(SaveUserPort.class);
    private final SaveUserOauthPort saveUserOauthPort = mock(SaveUserOauthPort.class);
    private final SaveUserProfilePort saveUserProfilePort = mock(SaveUserProfilePort.class);
    private final IssueAccessTokenPort issueAccessTokenPort = mock(IssueAccessTokenPort.class);

    private final LoginService service = new LoginService(
            exchangeTossTokenPort,
            loadTossUserPort,
            exchangeGoogleTokenPort,
            loadGoogleUserPort,
            loadUserOauthPort,
            saveUserPort,
            saveUserOauthPort,
            saveUserProfilePort,
            issueAccessTokenPort
    );

    @Test
    void login_기존회원이면_회원생성없이_서비스토큰을_발급한다() {
        LoginCommand command = AuthFixture.createTossLoginCommand();
        TossAccessToken tossAccessToken = AuthFixture.createTossAccessToken();
        TossAuthenticatedUser tossUser = AuthFixture.createTossAuthenticatedUser();

        User existingUser = User.create();
        UserOauth existingUserOauth = UserOauth.create(
                existingUser.getId(),
                OauthProvider.TOSS,
                ProviderUserId.of(tossUser.userKey())
        );

        when(exchangeTossTokenPort.exchange(command.authorizationCode(), command.referrer())).thenReturn(tossAccessToken);
        when(loadTossUserPort.load(tossAccessToken.accessToken())).thenReturn(tossUser);
        when(loadUserOauthPort.findByProviderAndProviderUserId(OauthProvider.TOSS, ProviderUserId.of(tossUser.userKey())))
                .thenReturn(Optional.of(existingUserOauth));
        when(issueAccessTokenPort.issue(existingUser.getId())).thenReturn("service-jwt-token");

        AuthTokenView result = service.login(command);

        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.accessToken()).isEqualTo("service-jwt-token");

        InOrder inOrder = inOrder(exchangeTossTokenPort, loadTossUserPort, loadUserOauthPort, issueAccessTokenPort);
        inOrder.verify(exchangeTossTokenPort).exchange(command.authorizationCode(), command.referrer());
        inOrder.verify(loadTossUserPort).load(tossAccessToken.accessToken());
        inOrder.verify(loadUserOauthPort).findByProviderAndProviderUserId(
                OauthProvider.TOSS,
                ProviderUserId.of(tossUser.userKey())
        );
        inOrder.verify(issueAccessTokenPort).issue(existingUser.getId());

        verify(saveUserPort, never()).save(any());
        verify(saveUserOauthPort, never()).save(any());
        verify(saveUserProfilePort, never()).save(any());
    }

    @Test
    void login_신규회원이면_유저와_프로필을_생성하고_이름을_닉네임으로_사용한다() {
        LoginCommand command = AuthFixture.createTossLoginCommand();
        TossAccessToken tossAccessToken = AuthFixture.createTossAccessToken();
        TossAuthenticatedUser tossUser = AuthFixture.createTossAuthenticatedUser();

        when(exchangeTossTokenPort.exchange(command.authorizationCode(), command.referrer())).thenReturn(tossAccessToken);
        when(loadTossUserPort.load(tossAccessToken.accessToken())).thenReturn(tossUser);
        when(loadUserOauthPort.findByProviderAndProviderUserId(OauthProvider.TOSS, ProviderUserId.of(tossUser.userKey())))
                .thenReturn(Optional.empty());
        when(issueAccessTokenPort.issue(any(UserId.class))).thenReturn("new-user-jwt");

        AuthTokenView result = service.login(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserOauth> userOauthCaptor = ArgumentCaptor.forClass(UserOauth.class);
        ArgumentCaptor<UserProfile> userProfileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        ArgumentCaptor<UserId> issuedUserIdCaptor = ArgumentCaptor.forClass(UserId.class);

        verify(saveUserPort).save(userCaptor.capture());
        verify(saveUserOauthPort).save(userOauthCaptor.capture());
        verify(saveUserProfilePort).save(userProfileCaptor.capture());
        verify(issueAccessTokenPort).issue(issuedUserIdCaptor.capture());

        User savedUser = userCaptor.getValue();
        UserOauth savedUserOauth = userOauthCaptor.getValue();
        UserProfile savedUserProfile = userProfileCaptor.getValue();

        assertThat(result.accessToken()).isEqualTo("new-user-jwt");
        assertThat(savedUserOauth.getUserId()).isEqualTo(savedUser.getId());
        assertThat(savedUserOauth.getProvider()).isEqualTo(OauthProvider.TOSS);
        assertThat(savedUserOauth.getProviderUserId()).isEqualTo(ProviderUserId.of(tossUser.userKey()));
        assertThat(savedUserProfile.getUserId()).isEqualTo(savedUser.getId());
        assertThat(savedUserProfile.getNickname()).isEqualTo(tossUser.name());
        assertThat(issuedUserIdCaptor.getValue()).isEqualTo(savedUser.getId());
    }

    @Test
    void login_토스이름이_비어있으면_providerUserId기반_기본닉네임을_사용한다() {
        LoginCommand command = AuthFixture.createTossLoginCommand();
        TossAccessToken tossAccessToken = AuthFixture.createTossAccessToken();
        TossAuthenticatedUser tossUser = AuthFixture.createUnnamedTossAuthenticatedUser();

        when(exchangeTossTokenPort.exchange(command.authorizationCode(), command.referrer())).thenReturn(tossAccessToken);
        when(loadTossUserPort.load(tossAccessToken.accessToken())).thenReturn(tossUser);
        when(loadUserOauthPort.findByProviderAndProviderUserId(OauthProvider.TOSS, ProviderUserId.of(tossUser.userKey())))
                .thenReturn(Optional.empty());
        when(issueAccessTokenPort.issue(any(UserId.class))).thenReturn("fallback-jwt");

        service.login(command);

        ArgumentCaptor<UserProfile> userProfileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(saveUserProfilePort).save(userProfileCaptor.capture());

        assertThat(userProfileCaptor.getValue().getNickname()).isEqualTo("user-" + tossUser.userKey());
    }

    @Test
    void login_구글_신규회원이면_유저와_프로필을_생성한다() {
        LoginCommand command = AuthFixture.createGoogleLoginCommand();
        GoogleAccessToken googleAccessToken = AuthFixture.createGoogleAccessToken();
        GoogleAuthenticatedUser googleUser = AuthFixture.createGoogleAuthenticatedUser();

        when(exchangeGoogleTokenPort.exchange(command.authorizationCode())).thenReturn(googleAccessToken);
        when(loadGoogleUserPort.load(googleAccessToken.accessToken())).thenReturn(googleUser);
        when(loadUserOauthPort.findByProviderAndProviderUserId(OauthProvider.GOOGLE, ProviderUserId.of(googleUser.userKey())))
                .thenReturn(Optional.empty());
        when(issueAccessTokenPort.issue(any(UserId.class))).thenReturn("google-user-jwt");

        AuthTokenView result = service.login(command);

        ArgumentCaptor<UserOauth> userOauthCaptor = ArgumentCaptor.forClass(UserOauth.class);
        ArgumentCaptor<UserProfile> userProfileCaptor = ArgumentCaptor.forClass(UserProfile.class);

        verify(saveUserOauthPort).save(userOauthCaptor.capture());
        verify(saveUserProfilePort).save(userProfileCaptor.capture());

        assertThat(result.accessToken()).isEqualTo("google-user-jwt");
        assertThat(userOauthCaptor.getValue().getProvider()).isEqualTo(OauthProvider.GOOGLE);
        assertThat(userOauthCaptor.getValue().getProviderUserId()).isEqualTo(ProviderUserId.of(googleUser.userKey()));
        assertThat(userProfileCaptor.getValue().getNickname()).isEqualTo(googleUser.name());
    }
}
