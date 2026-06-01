package com.pium.domain.recommendation.model.search;

import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.productprofile.enumtype.TraitStrength;
import com.pium.domain.recommendation.exception.RecommendationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductSearchSpecTest {

    @Test
    void ProductSearchSpec은_리스트를_불변으로_보관하고_중복_enum을_제거한다() {
        List<ProductRiskTrait> blockedRiskTraits = new ArrayList<>();
        blockedRiskTraits.add(ProductRiskTrait.STRONG_EXFOLIATION_EFFECT);
        blockedRiskTraits.add(ProductRiskTrait.STRONG_EXFOLIATION_EFFECT);

        ProductSearchSpec spec = new ProductSearchSpec(
                List.of(new TraitRequirement(RecommendationTrait.HYDRATION_SUPPORT, TraitStrength.WEAK)),
                List.of(new TraitPreference(RecommendationTrait.BARRIER_SUPPORT, TraitPreference.Weight.MEDIUM)),
                List.of(),
                blockedRiskTraits,
                List.of(),
                List.of(),
                List.of(ProductCategory.TONER, ProductCategory.TONER),
                ProductSearchSpec.FallbackPolicy.RELAX_REQUIRED_TO_PREFERRED_KEEP_BLOCKED
        );
        blockedRiskTraits.add(ProductRiskTrait.HIGH_IRRITATION_RISK);

        assertThat(spec.blockedRiskTraits()).containsExactly(ProductRiskTrait.STRONG_EXFOLIATION_EFFECT);
        assertThat(spec.categoryHints()).containsExactly(ProductCategory.TONER);
        assertThatThrownBy(() -> spec.blockedRiskTraits().add(ProductRiskTrait.HIGH_IRRITATION_RISK))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void ProductSearchSpec은_필수값을_검증한다() {
        assertThatThrownBy(() -> new ProductSearchSpec(
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                ProductSearchSpec.FallbackPolicy.RELAX_PREFERRED_KEEP_BLOCKED
        )).isInstanceOf(RecommendationException.class);

        assertThatThrownBy(() -> new ProductSearchSpec(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null
        )).isInstanceOf(RecommendationException.class);
    }

    @Test
    void TraitRequirement는_필수값을_검증한다() {
        assertThatThrownBy(() -> new TraitRequirement(null, TraitStrength.WEAK))
                .isInstanceOf(RecommendationException.class);
    }

    @Test
    void GoalTraitBoost는_goal을_trim해서_보관한다() {
        GoalTraitBoost boost = new GoalTraitBoost(
                " Q11_3 ",
                RecommendationTrait.BRIGHTENING_SUPPORT,
                TraitPreference.Weight.MEDIUM
        );

        assertThat(boost.goal()).isEqualTo("Q11_3");
    }
}
