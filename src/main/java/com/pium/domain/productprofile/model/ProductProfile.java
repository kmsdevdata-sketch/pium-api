package com.pium.domain.productprofile.model;

import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.UsageStep;
import com.pium.domain.product.vo.ProductId;
import com.pium.domain.productprofile.enumtype.ActiveFamily;
import com.pium.domain.productprofile.enumtype.ProductIngredientGroup;
import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.productprofile.exception.ProductProfileErrorCode;
import com.pium.domain.productprofile.exception.ProductProfileException;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 상품 원본데이터가 변환과정을 거쳐 추천 엔진이 읽을 수 있는 형태로 변한된 결과
 *
 * @param productId : 어떤 상품의 프로파일인지 나타내는 상품 ID
 * @param category : 상품 카테고리
 * @param usageStep : 루틴 사용 단계 (추후 확장고려)
 * @param benefitTraits : 상품이 줄 수 있는 긍정적 기능/효과 목록
 * @param riskTraits : 특정 피부 상태에서 부담이 될 수 있는 위험 신호 목록
 * @param ingredientGroups : 전성분에서 추출한 성분군 목록
 * @param activeFamilies : 주요 기능성/활성 성분 계열 목록
 * @param evidenceSignals : benefit/risk/성분군 판단에 사용된 근거 목록
 * @param warnings : 한계 메세지
 */
public record ProductProfile(
        ProductId productId,
        ProductCategory category,
        UsageStep usageStep,
        List<ProductTraitSignal<RecommendationTrait>> benefitTraits,
        List<ProductTraitSignal<ProductRiskTrait>> riskTraits,
        List<ProductIngredientGroup> ingredientGroups,
        List<ActiveFamily> activeFamilies,
        List<EvidenceSignal> evidenceSignals,
        List<String> warnings
) {

    public ProductProfile {
        if (productId == null || category == null || usageStep == null) {
            throw new ProductProfileException(ProductProfileErrorCode.INVALID_PRODUCT_PROFILE);
        }

        benefitTraits = copyRequired(benefitTraits);
        riskTraits = copyRequired(riskTraits);
        ingredientGroups = distinctRequired(ingredientGroups);
        activeFamilies = distinctRequired(activeFamilies);
        evidenceSignals = copyRequired(evidenceSignals);
        warnings = normalizeWarnings(warnings);

        validateEvidenceSignals(evidenceSignals);
        validateEvidenceReferences(benefitTraits, evidenceSignals);
        validateEvidenceReferences(riskTraits, evidenceSignals);
    }

    public static ProductProfile of(
            ProductId productId,
            ProductCategory category,
            UsageStep usageStep,
            List<ProductTraitSignal<RecommendationTrait>> benefitTraits,
            List<ProductTraitSignal<ProductRiskTrait>> riskTraits,
            List<ProductIngredientGroup> ingredientGroups,
            List<ActiveFamily> activeFamilies,
            List<EvidenceSignal> evidenceSignals,
            List<String> warnings
    ) {
        return new ProductProfile(
                productId,
                category,
                usageStep,
                benefitTraits,
                riskTraits,
                ingredientGroups,
                activeFamilies,
                evidenceSignals,
                warnings
        );
    }

    private static <T> List<T> copyRequired(List<T> values) {
        if (values == null) {
            throw new ProductProfileException(ProductProfileErrorCode.INVALID_PRODUCT_PROFILE);
        }
        if (values.stream().anyMatch(Objects::isNull)) {
            throw new ProductProfileException(ProductProfileErrorCode.INVALID_PRODUCT_PROFILE);
        }
        return List.copyOf(values);
    }

    private static <T> List<T> distinctRequired(List<T> values) {
        if (values == null) {
            throw new ProductProfileException(ProductProfileErrorCode.INVALID_PRODUCT_PROFILE);
        }
        if (values.stream().anyMatch(Objects::isNull)) {
            throw new ProductProfileException(ProductProfileErrorCode.INVALID_PRODUCT_PROFILE);
        }
        return values.stream()
                .distinct()
                .toList();
    }

    private static List<String> normalizeWarnings(List<String> warnings) {
        if (warnings == null) {
            throw new ProductProfileException(ProductProfileErrorCode.INVALID_PRODUCT_PROFILE);
        }
        return warnings.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(warning -> !warning.isBlank())
                .distinct()
                .toList();
    }

    private static void validateEvidenceSignals(List<EvidenceSignal> evidenceSignals) {
        Set<String> ids = new HashSet<>();
        for (EvidenceSignal evidenceSignal : evidenceSignals) {
            if (!ids.add(evidenceSignal.id())) {
                throw new ProductProfileException(ProductProfileErrorCode.INVALID_EVIDENCE_SIGNAL);
            }
        }
    }

    private static void validateEvidenceReferences(
            List<? extends ProductTraitSignal<?>> traitSignals,
            List<EvidenceSignal> evidenceSignals
    ) {
        Set<String> evidenceIds = evidenceSignals.stream()
                .map(EvidenceSignal::id)
                .collect(java.util.stream.Collectors.toSet());

        boolean hasInvalidRef = traitSignals.stream()
                .flatMap(signal -> signal.evidenceRefs().stream())
                .anyMatch(ref -> !evidenceIds.contains(ref));

        if (hasInvalidRef) {
            throw new ProductProfileException(ProductProfileErrorCode.INVALID_EVIDENCE_REFERENCE);
        }
    }
}
