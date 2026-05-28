package com.pium.adapter.outbound.productprofile.openai;

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

/**
 * OpenAI가 생성하는 ProductProfile 초안.
 */
record OpenAiProductProfileDraft(
        List<BenefitTraitDraft> benefitTraits,
        List<RiskTraitDraft> riskTraits,
        List<ProductIngredientGroup> ingredientGroups,
        List<ActiveFamily> activeFamilies,
        List<EvidenceSignalDraft> evidenceSignals,
        List<String> warnings
) {

    ProductProfile toDomain(ProductId productId, ProductCategory category, UsageStep usageStep) {
        return ProductProfile.of(
                productId,
                category,
                usageStep,
                benefitTraits.stream()
                        .map(BenefitTraitDraft::toDomain)
                        .toList(),
                riskTraits.stream()
                        .map(RiskTraitDraft::toDomain)
                        .toList(),
                ingredientGroups,
                activeFamilies,
                evidenceSignals.stream()
                        .map(EvidenceSignalDraft::toDomain)
                        .toList(),
                warnings
        );
    }

    record BenefitTraitDraft(
            RecommendationTrait trait,
            TraitStrength strength,
            EvidenceConfidence confidence,
            List<String> evidenceRefs
    ) {

        ProductTraitSignal<RecommendationTrait> toDomain() {
            return ProductTraitSignal.of(trait, strength, confidence, evidenceRefs);
        }
    }

    record RiskTraitDraft(
            ProductRiskTrait trait,
            TraitStrength strength,
            EvidenceConfidence confidence,
            List<String> evidenceRefs
    ) {

        ProductTraitSignal<ProductRiskTrait> toDomain() {
            return ProductTraitSignal.of(trait, strength, confidence, evidenceRefs);
        }
    }

    record EvidenceSignalDraft(
            String id,
            EvidenceType type,
            EvidenceSourceField sourceField,
            String message,
            EvidenceConfidence confidence
    ) {

        EvidenceSignal toDomain() {
            return EvidenceSignal.of(id, type, sourceField, message, confidence);
        }
    }
}
