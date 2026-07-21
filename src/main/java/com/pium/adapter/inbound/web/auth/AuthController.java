package com.pium.adapter.inbound.web.auth;

import com.pium.adapter.inbound.response.ApiResponse;
import com.pium.application.auth.dto.AuthTokenView;
import com.pium.application.auth.provider.Login;
import com.pium.application.auth.service.AuthTokenService;
import jakarta.validation.Valid;
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
    private final AuthTokenService authTokenService;

    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthTokenView response = login.login(request.toCommand());
        return ApiResponse.ok(AuthTokenResponse.from(response));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthTokenView response = authTokenService.refresh(request.refreshToken());
        return ApiResponse.ok(AuthTokenResponse.from(response));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authTokenService.logout(request.refreshToken());
        return ApiResponse.ok();
    }
}
