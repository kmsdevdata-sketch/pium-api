package com.pium.domain.product.enumtype;

import com.pium.domain.product.exception.ProductErrorCode;
import com.pium.domain.product.exception.ProductException;

public enum FunctionalLabel {
    BRIGHTENING,
    WRINKLE_IMPROVEMENT,
    UV_PROTECTION,
    ACNE_PRONE_SKIN_RELIEF,
    BARRIER_FUNCTION_RECOVERY,
    NONE;

    public static FunctionalLabel of(String value) {
        if (value == null || value.isBlank()) {
            throw new ProductException(ProductErrorCode.INVALID_FUNCTIONAL_LABEL);
        }
        try {
            return FunctionalLabel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ProductException(ProductErrorCode.INVALID_FUNCTIONAL_LABEL);
        }
    }
}
