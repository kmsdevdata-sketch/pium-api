package com.pium.application.recommendation.service;

import com.pium.application.product.required.LoadProductPort;
import com.pium.application.recommendation.dto.ProductRecommendationDetailView;
import com.pium.application.recommendation.dto.ProductRecommendationListView;
import com.pium.application.recommendation.exception.RecommendationApplicationException;
import com.pium.application.recommendation.required.LoadRecommendationProductProfilePort;
import com.pium.application.skinanalysis.result.required.LoadSkinAnalysisResultPort;
import com.pium.application.skinanalysis.result.service.SkinAnalysisResultViewComposer;
import com.pium.domain.product.fixture.ProductFixture;
import com.pium.domain.product.model.Product;
import com.pium.domain.productprofile.enumtype.EvidenceConfidence;
import com.pium.domain.productprofile.enumtype.EvidenceSourceField;
import com.pium.domain.productprofile.enumtype.EvidenceType;
import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.productprofile.enumtype.TraitStrength;
import com.pium.domain.productprofile.fixture.ProductProfileFixture;
import com.pium.domain.productprofile.model.EvidenceSignal;
import com.pium.domain.productprofile.model.ProductProfile;
import com.pium.domain.productprofile.model.ProductTraitSignal;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetProductRecommendationServiceTest {

    private final LoadSkinAnalysisResultPort loadSkinAnalysisResultPort = mock(LoadSkinAnalysisResultPort.class);
    private final LoadRecommendationProductProfilePort loadRecommendationProductProfilePort = mock(LoadRecommendationProductProfilePort.class);
    private final LoadProductPort loadProductPort = mock(LoadProductPort.class);
    private final GetProductRecommendationService service = new GetProductRecommendationService(
            loadSkinAnalysisResultPort,
            loadRecommendationProductProfilePort,
            loadProductPort,
            new SkinAnalysisResultViewComposer()
    );

    @Test
    void getLatest_최신진단기반_추천목록을_조회한다() {
        UserId userId = UserId.of("user-1");
        Product product = ProductFixture.createProduct();
        ProductProfile profile = hydrationProfile(product);
        SkinAnalysisResult result = skinAnalysisResult(userId);

        when(loadSkinAnalysisResultPort.loadLatest(userId)).thenReturn(Optional.of(result));
        when(loadRecommendationProductProfilePort.loadCandidates(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(profile));
        when(loadProductPort.findById(product.getId())).thenReturn(Optional.of(product));

        ProductRecommendationListView response = service.getLatest(userId, "ALL");

        assertThat(response.analysisResultId()).isEqualTo(result.getId().value());
        assertThat(response.adDisclosure()).isEqualTo(ProductRecommendationTextComposer.AD_DISCLOSURE);
        assertThat(response.recommendationSummary().headline())
                .isEqualTo("건조 신호를 기준으로, 수분 케어 포인트와 사용 전 주의점을 함께 봤어요.");
        assertThat(response.topRecommendations()).hasSize(1);
        assertThat(response.topRecommendations().get(0).careTags()).contains("수분 충전");
        assertThat(response.topRecommendations().get(0).cautionPoints()).contains("향 성분 주의");
        assertThat(response.topRecommendations().get(0).recommendationReason()).contains("건조 신호");
    }

    @Test
    void getLatest_category가_있으면_해당_카테고리만_반환한다() {
        UserId userId = UserId.of("user-1");
        Product product = ProductFixture.createProduct();
        ProductProfile profile = hydrationProfile(product);

        when(loadSkinAnalysisResultPort.loadLatest(userId)).thenReturn(Optional.of(skinAnalysisResult(userId)));
        when(loadRecommendationProductProfilePort.loadCandidates(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(profile));
        when(loadProductPort.findById(product.getId())).thenReturn(Optional.of(product));

        ProductRecommendationListView response = service.getLatest(userId, "SUN_CARE");

        assertThat(response.topRecommendations()).isEmpty();
        assertThat(response.recommendations()).isEmpty();
        assertThat(response.filters().selectedCategory()).isEqualTo("SUN_CARE");
    }

    /**
     * TODO: 추천 상세 조회 시 cautionPoints 가 비어있는 문제 확인 필요
     *
     * 현재 확인된 사항
     * 1. ProductProfile.riskTraits 에는
     *    FRAGRANCE_OR_ALLERGEN_RISK 가 정상적으로 존재함
     *
     * 2. ProductProfile 생성 시 EvidenceSignal 및 RiskTrait 모두 정상 포함됨
     *
     * 3. response.cautionPoints() 는 빈 리스트로 반환됨
     *
     * 4. textComposer.cautionPointViews(candidate) 내부의
     *    appliedRisks(candidate) 결과가 빈 리스트임
     *
     * 5. appliedRisks() 는 candidate.penaltyRisks(),
     *    candidate.cautionRisks() 를 합치는 단순 로직임
     *
     * 의심 지점
     * - ScoredRecommendationCandidate 생성 과정에서
     *   ProductProfile.riskTraits -> AppliedRisk 변환이 누락되는지 확인
     *
     * - candidate.penaltyRisks(), candidate.cautionRisks()
     *   생성 규칙 확인
     *
     * - FRAGRANCE_OR_ALLERGEN_RISK 가 어떤 조건에서
     *   AppliedRisk 로 승격되는지 확인
     */
    @Test
    void getDetail_추천상품_상세를_조회한다() {
        UserId userId = UserId.of("user-1");
        Product product = ProductFixture.createProduct();
        ProductProfile profile = hydrationProfile(product);

        when(loadSkinAnalysisResultPort.loadLatest(userId)).thenReturn(Optional.of(skinAnalysisResult(userId)));
        when(loadRecommendationProductProfilePort.loadCandidates(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(profile));
        when(loadProductPort.findById(product.getId())).thenReturn(Optional.of(product));

        ProductRecommendationDetailView response = service.getDetail(userId, product.getId().value());

        assertThat(response.productId()).isEqualTo(product.getId().value());
        assertThat(response.reasonDetails()).extracting(ProductRecommendationDetailView.ReasonDetailView::title)
                .containsExactly("진단에서 본 점", "상품에서 확인한 점", "추천에 반영한 방식");
        assertThat(response.careTags()).extracting(ProductRecommendationDetailView.TagView::label)
                .contains("수분 충전");
        assertThat(response.cautionPoints()).extracting(ProductRecommendationDetailView.TagView::label)
                .contains("향 성분 주의");
    }

    @Test
    void getDetail_추천결과에_없는_상품이면_예외가_발생한다() {
        UserId userId = UserId.of("user-1");
        Product product = ProductFixture.createProduct();

        when(loadSkinAnalysisResultPort.loadLatest(userId)).thenReturn(Optional.of(skinAnalysisResult(userId)));
        when(loadRecommendationProductProfilePort.loadCandidates(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(ProductProfileFixture.createProductProfile(product.getId())));
        when(loadProductPort.findById(product.getId())).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.getDetail(userId, "missing-product"))
                .isInstanceOf(RecommendationApplicationException.class);
    }

    private SkinAnalysisResult skinAnalysisResult(UserId userId) {
        return SkinAnalysisResult.create(
                userId,
                List.of(
                        SkinMetricScore.of(SkinMetric.DRYNESS, 82),
                        SkinMetricScore.of(SkinMetric.BARRIER, 20),
                        SkinMetricScore.of(SkinMetric.OILINESS, 10),
                        SkinMetricScore.of(SkinMetric.BLEMISH_PRONENESS, 10),
                        SkinMetricScore.of(SkinMetric.SENSITIVITY, 45),
                        SkinMetricScore.of(SkinMetric.PIGMENTATION_TONE, 10),
                        SkinMetricScore.of(SkinMetric.AGING_SIGNS, 10)
                ),
                List.of("Q11_1")
        );
    }

    private ProductProfile hydrationProfile(Product product) {
        ProductTraitSignal<RecommendationTrait> benefitTrait = ProductTraitSignal.of(
                RecommendationTrait.HYDRATION_SUPPORT,
                TraitStrength.MODERATE,
                EvidenceConfidence.HIGH,
                List.of("ev_1")
        );
        ProductTraitSignal<ProductRiskTrait> riskTrait = ProductTraitSignal.of(
                ProductRiskTrait.FRAGRANCE_OR_ALLERGEN_RISK,
                TraitStrength.WEAK,
                EvidenceConfidence.MEDIUM,
                List.of("ev_2")
        );

        ProductProfile baseProfile = ProductProfileFixture.createProductProfile(product.getId());
        return ProductProfile.of(
                product.getId(),
                product.getCategory(),
                product.getUsageStep(),
                List.of(benefitTrait),
                List.of(riskTrait),
                baseProfile.ingredientGroups(),
                baseProfile.activeFamilies(),
                List.of(
                        EvidenceSignal.of(
                                "ev_1",
                                EvidenceType.INGREDIENT_GROUP,
                                EvidenceSourceField.INGREDIENTS,
                                "전성분에서 보습 성분군이 확인돼요.",
                                EvidenceConfidence.HIGH
                        ),
                        EvidenceSignal.of(
                                "ev_2",
                                EvidenceType.INGREDIENT_PRESENT,
                                EvidenceSourceField.INGREDIENTS,
                                "향 성분이 확인돼요.",
                                EvidenceConfidence.MEDIUM
                        )
                ),
                List.of()
        );
    }
}
