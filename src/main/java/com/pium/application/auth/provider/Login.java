package com.pium.application.auth.provider;

import com.pium.application.auth.dto.AuthTokenView;
import com.pium.application.auth.dto.LoginCommand;

public interface Login {

    AuthTokenView login(LoginCommand command);
}
