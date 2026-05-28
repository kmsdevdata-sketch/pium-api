package com.pium.adapter.inbound.web.admin.product;

import com.pium.application.productprofile.dto.ProductProfileView;

import java.util.List;

public record ProductProfileResponse(
        String productId,
        String category,
        String usageStep,
        List<BenefitTraitResponse> benefitTraits,
        List<RiskTraitResponse> riskTraits,
        List<String> ingredientGroups,
        List<String> activeFamilies,
        List<EvidenceSignalResponse> evidenceSignals,
        List<String> warnings
) {

    public static ProductProfileResponse from(ProductProfileView view) {
        return new ProductProfileResponse(
                view.productId(),
                view.category().name(),
                view.usageStep().name(),
                view.benefitTraits().stream()
                        .map(BenefitTraitResponse::from)
                        .toList(),
                view.riskTraits().stream()
                        .map(RiskTraitResponse::from)
                        .toList(),
                view.ingredientGroups().stream()
                        .map(Enum::name)
                        .toList(),
                view.activeFamilies().stream()
                        .map(Enum::name)
                        .toList(),
                view.evidenceSignals().stream()
                        .map(EvidenceSignalResponse::from)
                        .toList(),
                view.warnings()
        );
    }

    public record BenefitTraitResponse(
            String trait,
            String strength,
            String confidence,
            List<String> evidenceRefs
    ) {

        public static BenefitTraitResponse from(ProductProfileView.BenefitTraitView view) {
            return new BenefitTraitResponse(
                    view.trait().name(),
                    view.strength().name(),
                    view.confidence().name(),
                    view.evidenceRefs()
            );
        }
    }

    public record RiskTraitResponse(
            String trait,
            String strength,
            String confidence,
            List<String> evidenceRefs
    ) {

        public static RiskTraitResponse from(ProductProfileView.RiskTraitView view) {
            return new RiskTraitResponse(
                    view.trait().name(),
                    view.strength().name(),
                    view.confidence().name(),
                    view.evidenceRefs()
            );
        }
    }

    public record EvidenceSignalResponse(
            String id,
            String type,
            String sourceField,
            String message,
            String confidence
    ) {

        public static EvidenceSignalResponse from(ProductProfileView.EvidenceSignalView view) {
            return new EvidenceSignalResponse(
                    view.id(),
                    view.type().name(),
                    view.sourceField().name(),
                    view.message(),
                    view.confidence().name()
            );
        }
    }
}
