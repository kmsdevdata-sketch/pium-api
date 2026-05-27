package com.pium.application.auth.fixture;

import com.pium.adapter.inbound.web.auth.AuthenticatedUser;
import com.pium.application.auth.dto.LoginCommand;
import com.pium.application.auth.required.dto.GoogleAccessToken;
import com.pium.application.auth.required.dto.GoogleAuthenticatedUser;
import com.pium.application.auth.required.dto.TossAccessToken;
import com.pium.application.auth.required.dto.TossAuthenticatedUser;
import com.pium.domain.user.enumtype.OauthProvider;
import com.pium.domain.user.enumtype.UserRole;
import com.pium.domain.user.vo.UserId;

public final class AuthFixture {

    private AuthFixture() {
    }

    public static LoginCommand createTossLoginCommand() {
        return new LoginCommand(
                OauthProvider.TOSS,
                "auth-code-001",
                "DEFAULT"
        );
    }

    public static LoginCommand createGoogleLoginCommand() {
        return new LoginCommand(
                OauthProvider.GOOGLE,
                "google-auth-code-001",
                null
        );
    }

    public static TossAccessToken createTossAccessToken() {
        return new TossAccessToken(
                "toss-access-token",
                "toss-refresh-token",
                "Bearer",
                3600L
        );
    }

    public static TossAuthenticatedUser createTossAuthenticatedUser() {
        return new TossAuthenticatedUser("443731104", "피움사용자");
    }

    public static GoogleAccessToken createGoogleAccessToken() {
        return new GoogleAccessToken(
                "google-access-token",
                null,
                "Bearer",
                3600L,
                "google-id-token"
        );
    }

    public static GoogleAuthenticatedUser createGoogleAuthenticatedUser() {
        return new GoogleAuthenticatedUser("google-sub-001", "구글사용자");
    }

    public static TossAuthenticatedUser createUnnamedTossAuthenticatedUser() {
        return new TossAuthenticatedUser("443731104", "   ");
    }

    public static AuthenticatedUser createAuthenticatedUser(UserId userId) {
        return new AuthenticatedUser(userId.value(), "피움닉네임", UserRole.USER);
    }

    public static AuthenticatedUser createAdminAuthenticatedUser(UserId userId) {
        return new AuthenticatedUser(userId.value(), "피움닉네임", UserRole.ADMIN);
    }
}
