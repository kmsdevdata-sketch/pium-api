package com.pium.domain.productprofile.model;

import com.pium.domain.productprofile.enumtype.EvidenceConfidence;
import com.pium.domain.productprofile.enumtype.TraitStrength;
import com.pium.domain.productprofile.exception.ProductProfileErrorCode;
import com.pium.domain.productprofile.exception.ProductProfileException;

import java.util.List;
import java.util.Objects;

/**
 * 상품이 가진 하나의 trait 신호를 표현
 *
 * @param trait : 실제 trait enum 값 ex. BARRIER_SUPPORT, FRAGRANCE_OR_ALLERGEN_RISK
 * @param strength : 해당 trait의 강도
 * @param confidence : 해당 판단 신뢰도
 * @param evidenceRefs : 해당 trait 판단을 뒷받침 하는 evidence id 목록
 */

//ProductTraitSignal 은 추천,리스크 둘중하나를 표현해야되는데
//실제 trait enum 값이 trait제외하곤 동일해서 <T extends Enum<T>> 로 Enum타입 강제
public record ProductTraitSignal<T extends Enum<T>>(
        T trait,
        TraitStrength strength,
        EvidenceConfidence confidence,
        List<String> evidenceRefs
) {

    public ProductTraitSignal {
        if (trait == null || strength == null || confidence == null) {
            throw new ProductProfileException(ProductProfileErrorCode.INVALID_TRAIT_SIGNAL);
        }
        evidenceRefs = normalizeEvidenceRefs(evidenceRefs);
    }

    public static <T extends Enum<T>> ProductTraitSignal<T> of(
            T trait,
            TraitStrength strength,
            EvidenceConfidence confidence,
            List<String> evidenceRefs
    ) {
        return new ProductTraitSignal<>(trait, strength, confidence, evidenceRefs);
    }

    private static List<String> normalizeEvidenceRefs(List<String> evidenceRefs) {
        if (evidenceRefs == null || evidenceRefs.isEmpty()) {
            throw new ProductProfileException(ProductProfileErrorCode.INVALID_TRAIT_SIGNAL);
        }

        List<String> normalized = evidenceRefs.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(ref -> !ref.isBlank())
                .distinct()
                .toList();

        if (normalized.isEmpty()) {
            throw new ProductProfileException(ProductProfileErrorCode.INVALID_TRAIT_SIGNAL);
        }
        return normalized;
    }
}
