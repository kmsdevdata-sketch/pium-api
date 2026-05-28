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
 * @param trait
 * @param strength
 * @param confidence
 * @param evidenceRefs
 * @param <T>
 */
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
