package com.pium.application.auth.service;

import com.pium.application.auth.dto.AuthTokenView;
import com.pium.application.auth.dto.ExternalAuthenticatedUser;
import com.pium.application.auth.dto.LoginCommand;
import com.pium.application.auth.provider.Login;
import com.pium.application.auth.required.ExchangeGoogleTokenPort;
import com.pium.application.auth.required.ExchangeKakaoTokenPort;
import com.pium.application.auth.required.ExchangeTossTokenPort;
import com.pium.application.auth.required.IssueAccessTokenPort;
import com.pium.application.auth.required.LoadGoogleUserPort;
import com.pium.application.auth.required.LoadKakaoUserPort;
import com.pium.application.auth.required.LoadTossUserPort;
import com.pium.application.auth.required.LoadUserOauthPort;
import com.pium.application.auth.required.SaveUserOauthPort;
import com.pium.application.auth.required.SaveUserPort;
import com.pium.application.auth.required.SaveUserProfilePort;
import com.pium.application.auth.required.dto.GoogleAccessToken;
import com.pium.application.auth.required.dto.GoogleAuthenticatedUser;
import com.pium.application.auth.required.dto.KakaoAccessToken;
import com.pium.application.auth.required.dto.KakaoAuthenticatedUser;
import com.pium.application.auth.required.dto.TossAccessToken;
import com.pium.application.auth.required.dto.TossAuthenticatedUser;
import com.pium.domain.user.enumtype.OauthProvider;
import com.pium.domain.user.model.User;
import com.pium.domain.user.model.UserOauth;
import com.pium.domain.user.model.UserProfile;
import com.pium.domain.user.vo.ProviderUserId;
import com.pium.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService implements Login {

    private static final String BEARER = "Bearer";
    private static final String DEFAULT_PROFILE_NAME_PREFIX = "user-";

    private final ExchangeTossTokenPort exchangeTossTokenPort;
    private final LoadTossUserPort loadTossUserPort;
    private final ExchangeGoogleTokenPort exchangeGoogleTokenPort;
    private final LoadGoogleUserPort loadGoogleUserPort;
    private final ExchangeKakaoTokenPort exchangeKakaoTokenPort;
    private final LoadKakaoUserPort loadKakaoUserPort;
    private final LoadUserOauthPort loadUserOauthPort;
    private final SaveUserPort saveUserPort;
    private final SaveUserOauthPort saveUserOauthPort;
    private final SaveUserProfilePort saveUserProfilePort;
    private final IssueAccessTokenPort issueAccessTokenPort;

    @Override
    public AuthTokenView login(LoginCommand command) {
        ExternalAuthenticatedUser externalUser = authenticate(command);
        UserId userId = loadOrCreateUser(externalUser);
        String accessToken = issueAccessTokenPort.issue(userId);

        return new AuthTokenView(BEARER, accessToken);
    }

    private ExternalAuthenticatedUser authenticate(LoginCommand command) {
        return switch (command.provider()) {
            case TOSS -> authenticateWithToss(command);
            case GOOGLE -> authenticateWithGoogle(command);
            case KAKAO -> authenticateWithKakao(command);
        };
    }

    private ExternalAuthenticatedUser authenticateWithGoogle(LoginCommand command) {
        GoogleAccessToken token = exchangeGoogleTokenPort.exchange(command.authorizationCode(), command.clientType());

        GoogleAuthenticatedUser googleUser = loadGoogleUserPort.load(token.accessToken());

        return new ExternalAuthenticatedUser(
                OauthProvider.GOOGLE,
                ProviderUserId.of(googleUser.userKey()),
                googleUser.name()
        );
    }

    private ExternalAuthenticatedUser authenticateWithKakao(LoginCommand command) {
        KakaoAccessToken token = exchangeKakaoTokenPort.exchange(command.authorizationCode());

        KakaoAuthenticatedUser kakaoUser = loadKakaoUserPort.load(token.accessToken());

        return new ExternalAuthenticatedUser(
                OauthProvider.KAKAO,
                ProviderUserId.of(kakaoUser.userKey()),
                kakaoUser.name()
        );
    }

    private ExternalAuthenticatedUser authenticateWithToss(LoginCommand command) {
        TossAccessToken token = exchangeTossTokenPort.exchange(
                command.authorizationCode(),
                command.referrer()
        );

        TossAuthenticatedUser tossUser = loadTossUserPort.load(token.accessToken());

        return new ExternalAuthenticatedUser(
                OauthProvider.TOSS,
                ProviderUserId.of(tossUser.userKey()),
                tossUser.name()
        );
    }

    private UserId loadOrCreateUser(ExternalAuthenticatedUser externalUser) {
        return loadUserOauthPort.findByProviderAndProviderUserId(
                        externalUser.provider(),
                        externalUser.providerUserId()
                )
                .map(UserOauth::getUserId)
                .orElseGet(() -> createNewUser(externalUser));
    }

    private UserId createNewUser(ExternalAuthenticatedUser externalUser) {
        User user = User.create();
        saveUserPort.save(user);

        UserOauth userOauth = UserOauth.create(
                user.getId(),
                externalUser.provider(),
                externalUser.providerUserId()
        );
        saveUserOauthPort.save(userOauth);

        UserProfile userProfile = UserProfile.create(
                user.getId(),
                resolveNickname(externalUser),
                null
        );
        saveUserProfilePort.save(userProfile);

        return user.getId();
    }

    private String resolveNickname(ExternalAuthenticatedUser externalUser) {
        String name = externalUser.name();
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        return DEFAULT_PROFILE_NAME_PREFIX + externalUser.providerUserId().value();
    }
}
