package com.pium.domain.productprofile.fixture;

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

import java.util.List;

public final class ProductProfileFixture {

    private ProductProfileFixture() {
    }

    public static ProductProfile createProductProfile(ProductId productId) {
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
                productId,
                ProductCategory.LOTION_CREAM,
                UsageStep.MOISTURIZE,
                List.of(benefitTrait),
                List.of(riskTrait),
                List.of(ProductIngredientGroup.BARRIER_LIPID, ProductIngredientGroup.SOOTHING),
                List.of(ActiveFamily.CERAMIDE, ActiveFamily.PANTHENOL),
                List.of(evidence("ev_1"), evidence("ev_2")),
                List.of("정확한 함량은 알 수 없음")
        );
    }

    private static EvidenceSignal evidence(String id) {
        return EvidenceSignal.of(
                id,
                EvidenceType.INGREDIENT_PRESENT,
                EvidenceSourceField.INGREDIENTS,
                "근거 메시지",
                EvidenceConfidence.MEDIUM
        );
    }
}
