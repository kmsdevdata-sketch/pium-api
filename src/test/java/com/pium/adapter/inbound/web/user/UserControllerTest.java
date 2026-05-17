package com.pium.adapter.inbound.web.user;

import com.pium.application.auth.fixture.AuthFixture;
import com.pium.application.skinanalysis.result.dto.SkinAnalysisResultView;
import com.pium.application.skinanalysis.result.provided.GetSkinAnalysisResult;
import com.pium.application.user.bootstrap.dto.UserBootstrapView;
import com.pium.application.user.bootstrap.provided.GetUserBootstrap;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetUserBootstrap getUserBootstrap;

    @MockitoBean
    private GetSkinAnalysisResult getSkinAnalysisResult;

    @Test
    void getUserBootstrap_returnsApiResponse() throws Exception {
        given(getUserBootstrap.getUserBootstrap(UserId.of("user-test-001")))
                .willReturn(new UserBootstrapView(true, UserBootstrapView.EntryPoint.HOME));

        mockMvc.perform(get("/api/v1/users/me/bootstrap")
                        .with(user(AuthFixture.createAuthenticatedUser(UserId.of("user-test-001"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasDiagnosis").value(true))
                .andExpect(jsonPath("$.data.entryPoint").value("HOME"));
    }

    @Test
    void getLatestSkinAnalysisResult_returnsApiResponse() throws Exception {
        given(getSkinAnalysisResult.getLatest(UserId.of("user-test-001")))
                .willReturn(new SkinAnalysisResultView(
                        "result-001",
                        null,
                        "한줄 요약",
                        List.of(new SkinAnalysisResultView.SkinMetricScoreView("DRYNESS", 72, "HIGH")),
                        List.of(new SkinAnalysisResultView.CategoryDetailView(
                                "DRYNESS",
                                72,
                                "HIGH",
                                "상태 문장",
                                "인사이트 문장"
                        )),
                        "종합 해석"
                ));

        mockMvc.perform(get("/api/v1/users/me/skin-analysis-results/latest")
                        .with(user(AuthFixture.createAuthenticatedUser(UserId.of("user-test-001"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.resultId").value("result-001"))
                .andExpect(jsonPath("$.data.oneLiner").value("한줄 요약"))
                .andExpect(jsonPath("$.data.skinMetricScores[0].metricKey").value("DRYNESS"))
                .andExpect(jsonPath("$.data.categoryDetails[0].insight").value("인사이트 문장"))
                .andExpect(jsonPath("$.data.summary").value("종합 해석"));
    }
}
