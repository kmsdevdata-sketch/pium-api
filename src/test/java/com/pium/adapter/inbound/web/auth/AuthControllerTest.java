package com.pium.adapter.inbound.web.auth;

import com.pium.application.auth.dto.AuthTokenView;
import com.pium.application.auth.provider.Login;
import com.pium.application.auth.service.AuthTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Login login;

    @MockitoBean
    private AuthTokenService authTokenService;

    @Test
    void login_서비스토큰묶음을_반환한다() throws Exception {
        given(login.login(any())).willReturn(authTokenView("access-token", "refresh-token"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "GOOGLE",
                                  "authorizationCode": "auth-code",
                                  "clientType": "WEB"
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.accessTokenExpiresInSeconds").value(3600))
                .andExpect(jsonPath("$.data.refreshTokenExpiresInSeconds").value(1209600));
    }

    @Test
    void refresh_refreshToken으로_새토큰묶음을_반환한다() throws Exception {
        given(authTokenService.refresh("refresh-token"))
                .willReturn(authTokenView("new-access-token", "new-refresh-token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));

        verify(authTokenService).refresh("refresh-token");
    }

    @Test
    void logout_refreshToken을_폐기한다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authTokenService).logout("refresh-token");
    }

    @Test
    void login_필수값이_비어있으면_fieldErrors를_반환한다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "",
                                  "authorizationCode": ""
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_400_001"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").exists())
                .andExpect(jsonPath("$.error.fieldErrors[0].reason").exists());

        verifyNoInteractions(login);
    }

    private AuthTokenView authTokenView(String accessToken, String refreshToken) {
        return new AuthTokenView("Bearer", accessToken, refreshToken, 3600L, 1209600L);
    }
}
