package com.pium.application.auth.service;

import com.pium.application.auth.dto.AuthTokenView;
import com.pium.application.auth.dto.LoginCommand;
import com.pium.application.auth.provider.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService implements Login {


    @Override
    public AuthTokenView login(LoginCommand command) {
        return null;
    }
}
