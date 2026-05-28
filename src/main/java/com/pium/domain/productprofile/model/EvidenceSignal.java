package com.pium.domain.productprofile.model;

import com.pium.domain.productprofile.enumtype.EvidenceConfidence;
import com.pium.domain.productprofile.enumtype.EvidenceSourceField;
import com.pium.domain.productprofile.enumtype.EvidenceType;
import com.pium.domain.productprofile.exception.ProductProfileErrorCode;
import com.pium.domain.productprofile.exception.ProductProfileException;

/**
 * ProductProfile 안의 판단 근거 하나를 표현
 * AI가 어떤 trait를 만들었을 때 “왜 그렇게 판단했는지”를 남기기 위한 모델
 *
 * @param id
 * @param type
 * @param sourceField
 * @param message
 * @param confidence
 */
public record EvidenceSignal(
        String id,
        EvidenceType type,
        EvidenceSourceField sourceField,
        String message,
        EvidenceConfidence confidence
) {

    public EvidenceSignal {
        id = normalizeRequired(id);
        message = normalizeRequired(message);
        if (type == null || sourceField == null || confidence == null) {
            throw new ProductProfileException(ProductProfileErrorCode.INVALID_EVIDENCE_SIGNAL);
        }
    }

    public static EvidenceSignal of(
            String id,
            EvidenceType type,
            EvidenceSourceField sourceField,
            String message,
            EvidenceConfidence confidence
    ) {
        return new EvidenceSignal(id, type, sourceField, message, confidence);
    }

    private static String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            throw new ProductProfileException(ProductProfileErrorCode.INVALID_EVIDENCE_SIGNAL);
        }
        return value.trim();
    }
}
