package com.pium.domain.recommendation.engine.scoring;

import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.UsageStep;
import com.pium.domain.product.vo.ProductId;
import com.pium.domain.productprofile.enumtype.ActiveFamily;
import com.pium.domain.productprofile.enumtype.EvidenceConfidence;
import com.pium.domain.productprofile.enumtype.EvidenceSourceField;
import com.pium.domain.productprofile.enumtype.EvidenceType;
import com.pium.domain.productprofile.enumtype.ProductIngredientGroup;
import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.productprofile.enumtype.TraitStrength;
import com.pium.domain.productprofile.model.EvidenceSignal;
import com.pium.domain.productprofile.model.ProductProfile;
import com.pium.domain.productprofile.model.ProductTraitSignal;
import com.pium.domain.recommendation.enumtype.ScoreBand;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.recommendation.model.interpretation.RiskConstraint;
import com.pium.domain.recommendation.model.scoring.ScoredRecommendationCandidate;
import com.pium.domain.recommendation.model.search.GoalTraitBoost;
import com.pium.domain.recommendation.model.search.ProductSearchSpec;
import com.pium.domain.recommendation.model.search.TraitPreference;
import com.pium.domain.recommendation.model.search.TraitRequirement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecommendationPolicyTest {

    private final RecommendationPolicy policy = new RecommendationPolicy();

    @Test
    void score_조건과_상품프로파일을_비교해_점수화한다() {
        ProductProfile profile = productProfile(
                ProductId.of("product-1"),
                ProductCategory.ESSENCE_SERUM,
                List.of(
                        benefit(RecommendationTrait.HYDRATION_SUPPORT, TraitStrength.MODERATE),
                        benefit(RecommendationTrait.BARRIER_SUPPORT, TraitStrength.WEAK),
                        benefit(RecommendationTrait.BRIGHTENING_SUPPORT, TraitStrength.MODERATE)
                ),
                List.of(risk(ProductRiskTrait.FRAGRANCE_OR_ALLERGEN_RISK, TraitStrength.MODERATE))
        );

        ProductSearchSpec spec = new ProductSearchSpec(
                List.of(new TraitRequirement(RecommendationTrait.HYDRATION_SUPPORT, TraitStrength.WEAK)),
                List.of(new TraitPreference(RecommendationTrait.BARRIER_SUPPORT, TraitPreference.Weight.MEDIUM)),
                List.of(new GoalTraitBoost(
                        "Q11_3",
                        RecommendationTrait.BRIGHTENING_SUPPORT,
                        TraitPreference.Weight.MEDIUM
                )),
                List.of(),
                List.of(ProductRiskTrait.FRAGRANCE_OR_ALLERGEN_RISK),
                List.of(),
                List.of(ProductCategory.ESSENCE_SERUM),
                ProductSearchSpec.FallbackPolicy.RELAX_REQUIRED_TO_PREFERRED_KEEP_BLOCKED
        );

        Optional<ScoredRecommendationCandidate> result = policy.score(spec, profile);

        assertThat(result).isPresent();
        assertThat(result.get().score()).isEqualTo(55);
        assertThat(result.get().scoreBand()).isEqualTo(ScoreBand.MEDIUM);
        assertThat(result.get().matchedRequiredTraits()).hasSize(1);
        assertThat(result.get().matchedPreferredTraits()).hasSize(1);
        assertThat(result.get().matchedGoalTraits()).hasSize(1);
        assertThat(result.get().penaltyRisks()).hasSize(1);
        assertThat(result.get().cautionRisks()).isEmpty();
    }

    @Test
    void score_blockedRisk가_있으면_후보에서_제외한다() {
        ProductProfile profile = productProfile(
                ProductId.of("product-1"),
                ProductCategory.ESSENCE_SERUM,
                List.of(benefit(RecommendationTrait.HYDRATION_SUPPORT, TraitStrength.MODERATE)),
                List.of(risk(ProductRiskTrait.STRONG_EXFOLIATION_EFFECT, TraitStrength.STRONG))
        );

        ProductSearchSpec spec = new ProductSearchSpec(
                List.of(),
                List.of(),
                List.of(),
                List.of(ProductRiskTrait.STRONG_EXFOLIATION_EFFECT),
                List.of(),
                List.of(),
                List.of(),
                ProductSearchSpec.FallbackPolicy.RELAX_PREFERRED_KEEP_BLOCKED
        );

        Optional<ScoredRecommendationCandidate> result = policy.score(spec, profile);

        assertThat(result).isEmpty();
    }

    @Test
    void score_후보목록은_점수_내림차순으로_정렬한다() {
        ProductProfile highScoreProfile = productProfile(
                ProductId.of("product-high"),
                ProductCategory.ESSENCE_SERUM,
                List.of(
                        benefit(RecommendationTrait.HYDRATION_SUPPORT, TraitStrength.MODERATE),
                        benefit(RecommendationTrait.BARRIER_SUPPORT, TraitStrength.WEAK)
                ),
                List.of()
        );
        ProductProfile lowScoreProfile = productProfile(
                ProductId.of("product-low"),
                ProductCategory.ETC,
                List.of(benefit(RecommendationTrait.BARRIER_SUPPORT, TraitStrength.WEAK)),
                List.of()
        );

        ProductSearchSpec spec = new ProductSearchSpec(
                List.of(new TraitRequirement(RecommendationTrait.HYDRATION_SUPPORT, TraitStrength.WEAK)),
                List.of(new TraitPreference(RecommendationTrait.BARRIER_SUPPORT, TraitPreference.Weight.MEDIUM)),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(ProductCategory.ESSENCE_SERUM),
                ProductSearchSpec.FallbackPolicy.RELAX_REQUIRED_TO_PREFERRED_KEEP_BLOCKED
        );

        List<ScoredRecommendationCandidate> result = policy.score(
                spec,
                List.of(lowScoreProfile, highScoreProfile)
        );

        assertThat(result)
                .extracting(candidate -> candidate.productId().value())
                .containsExactly("product-high", "product-low");
    }

    @Test
    void score_null_입력은_허용하지_않는다() {
        ProductSearchSpec spec = new ProductSearchSpec(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                ProductSearchSpec.FallbackPolicy.RELAX_PREFERRED_KEEP_BLOCKED
        );

        assertThatThrownBy(() -> policy.score(null, List.of()))
                .isInstanceOf(RecommendationException.class);
        assertThatThrownBy(() -> policy.score(spec, (ProductProfile) null))
                .isInstanceOf(RecommendationException.class);
        assertThatThrownBy(() -> policy.score(spec, Collections.singletonList(null)))
                .isInstanceOf(RecommendationException.class);
    }

    private ProductProfile productProfile(
            ProductId productId,
            ProductCategory category,
            List<ProductTraitSignal<RecommendationTrait>> benefitTraits,
            List<ProductTraitSignal<ProductRiskTrait>> riskTraits
    ) {
        return ProductProfile.of(
                productId,
                category,
                UsageStep.MOISTURIZE,
                benefitTraits,
                riskTraits,
                List.of(ProductIngredientGroup.HUMECTANT),
                List.of(ActiveFamily.NIACINAMIDE),
                List.of(evidence("ev_1")),
                List.of()
        );
    }

    private ProductTraitSignal<RecommendationTrait> benefit(
            RecommendationTrait trait,
            TraitStrength strength
    ) {
        return ProductTraitSignal.of(
                trait,
                strength,
                EvidenceConfidence.MEDIUM,
                List.of("ev_1")
        );
    }

    private ProductTraitSignal<ProductRiskTrait> risk(
            ProductRiskTrait trait,
            TraitStrength strength
    ) {
        return ProductTraitSignal.of(
                trait,
                strength,
                EvidenceConfidence.MEDIUM,
                List.of("ev_1")
        );
    }

    private EvidenceSignal evidence(String id) {
        return EvidenceSignal.of(
                id,
                EvidenceType.INGREDIENT_PRESENT,
                EvidenceSourceField.INGREDIENTS,
                "근거",
                EvidenceConfidence.MEDIUM
        );
    }
}
