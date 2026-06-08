package com.pium.adapter.inbound.web.auth;

import com.pium.application.auth.dto.LoginCommand;
import com.pium.application.auth.dto.OauthClientType;
import com.pium.domain.user.enumtype.OauthProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest {

    @Test
    void toCommand_clientType이_없으면_ADMIN을_기본값으로_사용한다() {
        LoginRequest request = new LoginRequest("GOOGLE", "auth-code", null, null);

        LoginCommand command = request.toCommand();

        assertThat(command.provider()).isEqualTo(OauthProvider.GOOGLE);
        assertThat(command.authorizationCode()).isEqualTo("auth-code");
        assertThat(command.clientType()).isEqualTo(OauthClientType.ADMIN);
    }

    @Test
    void toCommand_clientType_WEB을_변환한다() {
        LoginRequest request = new LoginRequest("GOOGLE", "auth-code", null, "WEB");

        LoginCommand command = request.toCommand();

        assertThat(command.clientType()).isEqualTo(OauthClientType.WEB);
    }
}
