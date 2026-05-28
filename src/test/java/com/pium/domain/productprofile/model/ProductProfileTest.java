package com.pium.domain.productprofile.model;

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
import com.pium.domain.productprofile.exception.ProductProfileException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductProfileTest {

    @Test
    void 상품프로파일_생성_검증() {
        ProductProfile profile = createProfile();

        assertThat(profile.productId()).isEqualTo(ProductId.of("product-1"));
        assertThat(profile.category()).isEqualTo(ProductCategory.LOTION_CREAM);
        assertThat(profile.usageStep()).isEqualTo(UsageStep.MOISTURIZE);
        assertThat(profile.benefitTraits()).hasSize(1);
        assertThat(profile.riskTraits()).hasSize(1);
        assertThat(profile.ingredientGroups()).containsExactly(
                ProductIngredientGroup.BARRIER_LIPID,
                ProductIngredientGroup.SOOTHING
        );
        assertThat(profile.activeFamilies()).containsExactly(ActiveFamily.CERAMIDE, ActiveFamily.PANTHENOL);
        assertThat(profile.warnings()).containsExactly("정확한 함량은 알 수 없음");
    }

    @Test
    void trait가_존재하지_않는_evidence를_참조하면_예외가_발생한다() {
        ProductTraitSignal<RecommendationTrait> invalidTrait = ProductTraitSignal.of(
                RecommendationTrait.BARRIER_SUPPORT,
                TraitStrength.MODERATE,
                EvidenceConfidence.MEDIUM,
                List.of("missing")
        );

        assertThatThrownBy(() -> ProductProfile.of(
                ProductId.of("product-1"),
                ProductCategory.LOTION_CREAM,
                UsageStep.MOISTURIZE,
                List.of(invalidTrait),
                List.of(),
                List.of(),
                List.of(),
                List.of(evidence("ev_1")),
                List.of()
        )).isInstanceOf(ProductProfileException.class);
    }

    @Test
    void evidence_id가_중복되면_예외가_발생한다() {
        assertThatThrownBy(() -> ProductProfile.of(
                ProductId.of("product-1"),
                ProductCategory.LOTION_CREAM,
                UsageStep.MOISTURIZE,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(evidence("ev_1"), evidence("ev_1")),
                List.of()
        )).isInstanceOf(ProductProfileException.class);
    }

    @Test
    void 리스트_필드는_불변으로_보관된다() {
        List<ProductIngredientGroup> groups = new ArrayList<>();
        groups.add(ProductIngredientGroup.BARRIER_LIPID);

        ProductProfile profile = ProductProfile.of(
                ProductId.of("product-1"),
                ProductCategory.LOTION_CREAM,
                UsageStep.MOISTURIZE,
                List.of(),
                List.of(),
                groups,
                List.of(),
                List.of(),
                List.of()
        );
        groups.add(ProductIngredientGroup.SOOTHING);

        assertThat(profile.ingredientGroups()).containsExactly(ProductIngredientGroup.BARRIER_LIPID);
        assertThatThrownBy(() -> profile.ingredientGroups().add(ProductIngredientGroup.SOOTHING))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void trait_signal은_근거참조를_필수로_가진다() {
        assertThatThrownBy(() -> ProductTraitSignal.of(
                RecommendationTrait.HYDRATION_SUPPORT,
                TraitStrength.WEAK,
                EvidenceConfidence.MEDIUM,
                List.of()
        )).isInstanceOf(ProductProfileException.class);
    }

    private ProductProfile createProfile() {
        ProductTraitSignal<RecommendationTrait> benefitTrait = ProductTraitSignal.of(
                RecommendationTrait.BARRIER_SUPPORT,
                TraitStrength.MODERATE,
                EvidenceConfidence.MEDIUM,
                List.of("ev_1")
        );
        ProductTraitSignal<ProductRiskTrait> riskTrait = ProductTraitSignal.of(
                ProductRiskTrait.FRAGRANCE_OR_ALLERGEN_RISK,
                TraitStrength.WEAK,
                EvidenceConfidence.LOW,
                List.of("ev_2")
        );

        return ProductProfile.of(
                ProductId.of("product-1"),
                ProductCategory.LOTION_CREAM,
                UsageStep.MOISTURIZE,
                List.of(benefitTrait),
                List.of(riskTrait),
                List.of(
                        ProductIngredientGroup.BARRIER_LIPID,
                        ProductIngredientGroup.SOOTHING,
                        ProductIngredientGroup.SOOTHING
                ),
                List.of(ActiveFamily.CERAMIDE, ActiveFamily.PANTHENOL, ActiveFamily.PANTHENOL),
                List.of(evidence("ev_1"), evidence("ev_2")),
                List.of("정확한 함량은 알 수 없음", "정확한 함량은 알 수 없음")
        );
    }

    private EvidenceSignal evidence(String id) {
        return EvidenceSignal.of(
                id,
                EvidenceType.INGREDIENT_PRESENT,
                EvidenceSourceField.INGREDIENTS,
                "근거 메시지",
                EvidenceConfidence.MEDIUM
        );
    }
}
