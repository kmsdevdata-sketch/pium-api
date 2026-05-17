package com.pium.adapter.inbound.web.user;

import com.pium.application.auth.fixture.AuthFixture;
import com.pium.application.skinanalysis.result.dto.SkinAnalysisResultListView;
import com.pium.application.skinanalysis.result.dto.SkinAnalysisResultView;
import com.pium.application.skinanalysis.result.provided.GetSkinAnalysisResult;
import com.pium.application.user.home.dto.UserHomeView;
import com.pium.application.user.home.provided.GetUserHome;
import com.pium.application.user.bootstrap.dto.UserBootstrapView;
import com.pium.application.user.bootstrap.provided.GetUserBootstrap;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
    private GetUserHome getUserHome;

    @MockitoBean
    private GetUserBootstrap getUserBootstrap;

    @MockitoBean
    private GetSkinAnalysisResult getSkinAnalysisResult;

    @Test
    void listSkinAnalysisResults_returnsApiResponse() throws Exception {
        given(getSkinAnalysisResult.list(UserId.of("user-test-001")))
                .willReturn(new SkinAnalysisResultListView(
                        2L,
                        List.of(
                                new SkinAnalysisResultListView.ItemView(
                                        "result-001",
                                        LocalDateTime.of(2026, 5, 17, 17, 17, 30),
                                        "트러블과 건조 경향이 상대적으로 도드라져 보여요.",
                                        new SkinAnalysisResultListView.TopMetricView("BLEMISH_PRONENESS", "트러블", "HIGH")
                                )
                        )
                ));

        mockMvc.perform(get("/api/v1/users/me/skin-analysis-results")
                        .with(user(AuthFixture.createAuthenticatedUser(UserId.of("user-test-001"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.historyCount").value(2))
                .andExpect(jsonPath("$.data.results[0].resultId").value("result-001"))
                .andExpect(jsonPath("$.data.results[0].createdAt").value("2026-05-17T08:17:30Z"))
                .andExpect(jsonPath("$.data.results[0].oneLiner").value("트러블과 건조 경향이 상대적으로 도드라져 보여요."))
                .andExpect(jsonPath("$.data.results[0].topMetric.label").value("트러블"))
                .andExpect(jsonPath("$.data.results[0].topMetric.level").value("HIGH"));
    }

    @Test
    void getUserHome_returnsApiResponse() throws Exception {
        given(getUserHome.getUserHome(UserId.of("user-test-001")))
                .willReturn(new UserHomeView(
                        12L,
                        new UserHomeView.LatestDiagnosisView(
                                "result-001",
                                LocalDateTime.of(2026, 5, 17, 15, 12, 41),
                                "수분 부족과 장벽 흔들림을 먼저 다독이면 좋아요.",
                                List.of(
                                        new UserHomeView.ResultMetricPreviewView("DRYNESS", "건조", 82, "HIGH"),
                                        new UserHomeView.ResultMetricPreviewView("BARRIER", "장벽", 65, "MEDIUM"),
                                        new UserHomeView.ResultMetricPreviewView("OILINESS", "유분", 58, "MEDIUM")
                                )
                        )
                ));

        mockMvc.perform(get("/api/v1/users/me/home")
                        .with(user(AuthFixture.createAuthenticatedUser(UserId.of("user-test-001"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.historyCount").value(12))
                .andExpect(jsonPath("$.data.latestDiagnosis.id").value("result-001"))
                .andExpect(jsonPath("$.data.latestDiagnosis.createdAt").value("2026-05-17T06:12:41Z"))
                .andExpect(jsonPath("$.data.latestDiagnosis.previewMetrics[0].key").value("DRYNESS"))
                .andExpect(jsonPath("$.data.latestDiagnosis.previewMetrics[1].level").value("MEDIUM"));
    }

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
                        LocalDateTime.of(2026, 5, 17, 15, 12, 41),
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
                .andExpect(jsonPath("$.data.createdAt").value("2026-05-17T06:12:41Z"))
                .andExpect(jsonPath("$.data.oneLiner").value("한줄 요약"))
                .andExpect(jsonPath("$.data.skinMetricScores[0].metricKey").value("DRYNESS"))
                .andExpect(jsonPath("$.data.categoryDetails[0].insight").value("인사이트 문장"))
                .andExpect(jsonPath("$.data.summary").value("종합 해석"));
    }
}
