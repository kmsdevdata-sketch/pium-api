package com.pium.domain.recommendation.engine;

import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.productprofile.enumtype.TraitStrength;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.recommendation.model.GoalConflictNotice;
import com.pium.domain.recommendation.model.GoalNeed;
import com.pium.domain.recommendation.model.GoalTraitBoost;
import com.pium.domain.recommendation.model.ProductSearchSpec;
import com.pium.domain.recommendation.model.RiskConstraint;
import com.pium.domain.recommendation.model.SkinInterpretation;
import com.pium.domain.recommendation.model.SkinNeed;
import com.pium.domain.recommendation.model.TraitPreference;
import com.pium.domain.recommendation.model.TraitRequirement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductSearchSpecGeneratorTest {

    private final ProductSearchSpecGenerator generator = new ProductSearchSpecGenerator();

    @Test
    void generate_피부해석결과를_상품검색조건으로_변환한다() {
        SkinInterpretation interpretation = new SkinInterpretation(
                "result-1",
                SkinInterpretation.RoutineIntent.SOOTHING_FIRST,
                List.of(new SkinNeed(
                        RecommendationTrait.SOOTHING_SUPPORT,
                        SkinNeed.Intensity.REQUIRED,
                        "SENSITIVITY_HIGH"
                )),
                List.of(new SkinNeed(
                        RecommendationTrait.BARRIER_SUPPORT,
                        SkinNeed.Intensity.PREFERRED,
                        "BARRIER_MID"
                )),
                List.of(
                        new RiskConstraint(
                                ProductRiskTrait.FRAGRANCE_OR_ALLERGEN_RISK,
                                RiskConstraint.Policy.CAUTION,
                                "BARRIER_MID"
                        ),
                        new RiskConstraint(
                                ProductRiskTrait.FRAGRANCE_OR_ALLERGEN_RISK,
                                RiskConstraint.Policy.SOFT_PENALTY,
                                "SENSITIVITY_HIGH"
                        ),
                        new RiskConstraint(
                                ProductRiskTrait.STRONG_ACTIVE_RISK,
                                RiskConstraint.Policy.SOFT_PENALTY,
                                "SENSITIVITY_HIGH"
                        ),
                        new RiskConstraint(
                                ProductRiskTrait.STRONG_EXFOLIATION_EFFECT,
                                RiskConstraint.Policy.HARD_BLOCK,
                                "BARRIER_HIGH"
                        )
                ),
                List.of(new GoalNeed(
                        "Q11_3",
                        RecommendationTrait.BRIGHTENING_SUPPORT,
                        GoalNeed.Boost.MEDIUM
                )),
                List.of(new GoalConflictNotice(
                        "Q11_3",
                        "SENSITIVITY_HIGH",
                        "톤 케어 목표는 반영하되 저자극 톤 케어를 우선합니다."
                ))
        );

        ProductSearchSpec spec = generator.generate(interpretation);

        assertThat(spec.requiredTraits())
                .extracting(TraitRequirement::trait)
                .containsExactly(RecommendationTrait.SOOTHING_SUPPORT);
        assertThat(spec.requiredTraits())
                .extracting(TraitRequirement::minStrength)
                .containsExactly(TraitStrength.WEAK);
        assertThat(spec.preferredTraits())
                .extracting(TraitPreference::trait)
                .containsExactly(RecommendationTrait.BARRIER_SUPPORT);
        assertThat(spec.goalBoostTraits())
                .extracting(GoalTraitBoost::trait)
                .containsExactly(RecommendationTrait.BRIGHTENING_SUPPORT);
        assertThat(spec.blockedRiskTraits()).containsExactly(ProductRiskTrait.STRONG_EXFOLIATION_EFFECT);
        assertThat(spec.penaltyRiskTraits())
                .containsExactlyInAnyOrder(
                        ProductRiskTrait.FRAGRANCE_OR_ALLERGEN_RISK,
                        ProductRiskTrait.STRONG_ACTIVE_RISK
                );
        assertThat(spec.cautionRiskTraits()).isEmpty();
        assertThat(spec.categoryHints())
                .containsExactly(
                        ProductCategory.TONER,
                        ProductCategory.ESSENCE_SERUM,
                        ProductCategory.LOTION_CREAM,
                        ProductCategory.MASK_PACK
                );
        assertThat(spec.fallbackPolicy())
                .isEqualTo(ProductSearchSpec.FallbackPolicy.RELAX_REQUIRED_TO_PREFERRED_KEEP_BLOCKED);
    }

    @Test
    void generate_primaryNeed가_없으면_preferred만_완화하는_fallback을_사용한다() {
        SkinInterpretation interpretation = new SkinInterpretation(
                "result-1",
                SkinInterpretation.RoutineIntent.BASIC_BALANCE,
                List.of(),
                List.of(new SkinNeed(
                        RecommendationTrait.HYDRATION_SUPPORT,
                        SkinNeed.Intensity.PREFERRED,
                        "DRYNESS_MID"
                )),
                List.of(),
                List.of(),
                List.of()
        );

        ProductSearchSpec spec = generator.generate(interpretation);

        assertThat(spec.requiredTraits()).isEmpty();
        assertThat(spec.preferredTraits())
                .extracting(TraitPreference::trait)
                .containsExactly(RecommendationTrait.HYDRATION_SUPPORT);
        assertThat(spec.fallbackPolicy())
                .isEqualTo(ProductSearchSpec.FallbackPolicy.RELAX_PREFERRED_KEEP_BLOCKED);
    }

    @Test
    void generate_null_해석결과는_허용하지_않는다() {
        assertThatThrownBy(() -> generator.generate(null))
                .isInstanceOf(RecommendationException.class);
    }
}
