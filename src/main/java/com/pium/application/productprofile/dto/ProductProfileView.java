package com.pium.application.productprofile.dto;

import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.UsageStep;
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
 * ProductProfile 생성/조회 결과 응답 모델.
 */
public record ProductProfileView(
        String productId,
        ProductCategory category,
        UsageStep usageStep,
        List<BenefitTraitView> benefitTraits,
        List<RiskTraitView> riskTraits,
        List<ProductIngredientGroup> ingredientGroups,
        List<ActiveFamily> activeFamilies,
        List<EvidenceSignalView> evidenceSignals,
        List<String> warnings
) {

    public static ProductProfileView from(ProductProfile productProfile) {
        return new ProductProfileView(
                productProfile.productId().value(),
                productProfile.category(),
                productProfile.usageStep(),
                productProfile.benefitTraits().stream()
                        .map(BenefitTraitView::from)
                        .toList(),
                productProfile.riskTraits().stream()
                        .map(RiskTraitView::from)
                        .toList(),
                productProfile.ingredientGroups(),
                productProfile.activeFamilies(),
                productProfile.evidenceSignals().stream()
                        .map(EvidenceSignalView::from)
                        .toList(),
                productProfile.warnings()
        );
    }

    /**
     * 상품 benefit trait 응답 모델.
     */
    public record BenefitTraitView(
            RecommendationTrait trait,
            TraitStrength strength,
            EvidenceConfidence confidence,
            List<String> evidenceRefs
    ) {

        public static BenefitTraitView from(ProductTraitSignal<RecommendationTrait> signal) {
            return new BenefitTraitView(
                    signal.trait(),
                    signal.strength(),
                    signal.confidence(),
                    signal.evidenceRefs()
            );
        }
    }

    /**
     * 상품 risk trait 응답 모델.
     */
    public record RiskTraitView(
            ProductRiskTrait trait,
            TraitStrength strength,
            EvidenceConfidence confidence,
            List<String> evidenceRefs
    ) {

        public static RiskTraitView from(ProductTraitSignal<ProductRiskTrait> signal) {
            return new RiskTraitView(
                    signal.trait(),
                    signal.strength(),
                    signal.confidence(),
                    signal.evidenceRefs()
            );
        }
    }

    /**
     * ProductProfile 근거 응답 모델.
     */
    public record EvidenceSignalView(
            String id,
            EvidenceType type,
            EvidenceSourceField sourceField,
            String message,
            EvidenceConfidence confidence
    ) {

        public static EvidenceSignalView from(EvidenceSignal signal) {
            return new EvidenceSignalView(
                    signal.id(),
                    signal.type(),
                    signal.sourceField(),
                    signal.message(),
                    signal.confidence()
            );
        }
    }
}
