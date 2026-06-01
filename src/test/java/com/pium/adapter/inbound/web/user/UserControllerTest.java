package com.pium.adapter.inbound.web.user;

import com.pium.application.auth.fixture.AuthFixture;
import com.pium.application.recommendation.dto.ProductRecommendationDetailView;
import com.pium.application.recommendation.dto.ProductRecommendationItemView;
import com.pium.application.recommendation.dto.ProductRecommendationListView;
import com.pium.application.recommendation.provided.GetProductRecommendation;
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

    @MockitoBean
    private GetProductRecommendation getProductRecommendation;

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
                        "피움닉네임",
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
                .andExpect(jsonPath("$.data.userName").value("피움닉네임"))
                .andExpect(jsonPath("$.data.historyCount").value(12))
                .andExpect(jsonPath("$.data.latestDiagnosis.id").value("result-001"))
                .andExpect(jsonPath("$.data.latestDiagnosis.createdAt").value("2026-05-17T06:12:41Z"))
                .andExpect(jsonPath("$.data.latestDiagnosis.previewMetrics[0].key").value("DRYNESS"))
                .andExpect(jsonPath("$.data.latestDiagnosis.previewMetrics[1].level").value("MEDIUM"));
    }

    @Test
    void getUserBootstrap_returnsApiResponse() throws Exception {
        given(getUserBootstrap.getUserBootstrap(UserId.of("user-test-001")))
                .willReturn(new UserBootstrapView("피움닉네임", true));

        mockMvc.perform(get("/api/v1/users/me/bootstrap")
                        .with(user(AuthFixture.createAuthenticatedUser(UserId.of("user-test-001"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userName").value("피움닉네임"))
                .andExpect(jsonPath("$.data.hasDiagnosis").value(true))
                .andExpect(jsonPath("$.data.entryPoint").doesNotExist());
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

    @Test
    void getLatestRecommendations_returnsApiResponse() throws Exception {
        given(getProductRecommendation.getLatest(UserId.of("user-test-001"), "ALL"))
                .willReturn(new ProductRecommendationListView(
                        "result-001",
                        new ProductRecommendationListView.BasedOnView(
                                LocalDateTime.of(2026, 5, 17, 15, 12, 41),
                                "한줄 요약"
                        ),
                        new ProductRecommendationListView.RecommendationSummaryView(
                                "건조 신호를 기준으로, 수분 케어 포인트와 사용 전 주의점을 함께 봤어요.",
                                List.of("최근 진단에서 건조 신호가 높게 보여 수분을 먼저 채우는 방향이 필요해요."),
                                List.of("추천은 화장품 선택 참고용이며 의학적 진단이 아니에요.")
                        ),
                        "이 포스팅은 올리브영 쇼핑 큐레이터 활동의 일환으로, 구매 시 일정 금액의 수수료를 제공받습니다.",
                        new ProductRecommendationListView.FilterView(
                                "ALL",
                                List.of(new ProductRecommendationListView.CategoryFilterView("ALL", "전체"))
                        ),
                        List.of(recommendationItem(1)),
                        List.of()
                ));

        mockMvc.perform(get("/api/v1/users/me/recommendations")
                        .with(user(AuthFixture.createAuthenticatedUser(UserId.of("user-test-001"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.analysisResultId").value("result-001"))
                .andExpect(jsonPath("$.data.recommendationSummary.headline")
                        .value("건조 신호를 기준으로, 수분 케어 포인트와 사용 전 주의점을 함께 봤어요."))
                .andExpect(jsonPath("$.data.adDisclosure").value("이 포스팅은 올리브영 쇼핑 큐레이터 활동의 일환으로, 구매 시 일정 금액의 수수료를 제공받습니다."))
                .andExpect(jsonPath("$.data.topRecommendations[0].careTags[0]").value("수분 충전"))
                .andExpect(jsonPath("$.data.topRecommendations[0].cautionPoints[0]").value("향 성분 주의"));
    }

    @Test
    void getRecommendationDetail_returnsApiResponse() throws Exception {
        given(getProductRecommendation.getDetail(UserId.of("user-test-001"), "product-001"))
                .willReturn(new ProductRecommendationDetailView(
                        "product-001",
                        "피움랩",
                        "장벽 진정 크림",
                        24000,
                        "https://image.example/product.png",
                        "https://oliveyoung.example/products/1",
                        "LOTION_CREAM",
                        "로션/크림",
                        "MOISTURIZE",
                        "보습 단계",
                        "HIGH",
                        "잘 맞음",
                        List.of(new ProductRecommendationDetailView.ReasonDetailView(
                                "진단에서 본 점",
                                "최근 진단에서 건조 신호가 높게 보여 수분을 먼저 채우는 방향이 필요해요."
                        )),
                        List.of("건조 신호가 높아, 상품의 수분 케어 포인트가 확인된 후보를 우선 매칭했어요."),
                        List.of("향 성분 주의가 확인돼 현재 피부 상태에서는 사용 전 참고할 점으로 표시했어요."),
                        List.of(new ProductRecommendationDetailView.TagView("HYDRATION_SUPPORT", "수분 충전")),
                        List.of(new ProductRecommendationDetailView.TagView("FRAGRANCE_OR_ALLERGEN_RISK", "향 성분 주의")),
                        "이 포스팅은 올리브영 쇼핑 큐레이터 활동의 일환으로, 구매 시 일정 금액의 수수료를 제공받습니다."
                ));

        mockMvc.perform(get("/api/v1/users/me/recommendations/product-001")
                        .with(user(AuthFixture.createAuthenticatedUser(UserId.of("user-test-001"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value("product-001"))
                .andExpect(jsonPath("$.data.scoreBandLabel").value("잘 맞음"))
                .andExpect(jsonPath("$.data.reasonDetails[0].title").value("진단에서 본 점"))
                .andExpect(jsonPath("$.data.careTags[0].label").value("수분 충전"))
                .andExpect(jsonPath("$.data.cautionPoints[0].label").value("향 성분 주의"));
    }

    private ProductRecommendationItemView recommendationItem(int rank) {
        return new ProductRecommendationItemView(
                rank,
                "product-001",
                "피움랩",
                "장벽 진정 크림",
                24000,
                "https://image.example/product.png",
                "LOTION_CREAM",
                "로션/크림",
                "MOISTURIZE",
                "보습 단계",
                "HIGH",
                "잘 맞음",
                "건조 신호가 높아, 상품의 수분 케어 포인트가 확인된 후보를 우선 매칭했어요.",
                List.of("수분 충전"),
                List.of("향 성분 주의")
        );
    }
}
