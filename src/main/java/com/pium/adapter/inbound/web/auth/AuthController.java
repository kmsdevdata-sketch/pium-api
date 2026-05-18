package com.pium.adapter.inbound.web.auth;

import com.pium.adapter.inbound.response.ApiResponse;
import com.pium.application.auth.dto.AuthTokenView;
import com.pium.application.auth.provider.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final Login login;

    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(
            @RequestBody LoginRequest request
    ) {
        AuthTokenView response = login.login(request.toCommand());
        return ApiResponse.ok(AuthTokenResponse.from(response));
    }
}
