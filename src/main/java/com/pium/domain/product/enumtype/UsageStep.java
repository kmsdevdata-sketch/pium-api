package com.pium.domain.product.enumtype;

import com.pium.domain.product.exception.ProductErrorCode;
import com.pium.domain.product.exception.ProductException;

public enum UsageStep {
    CLEANSE,
    PREP,
    TREAT,
    MOISTURIZE,
    PROTECT,
    SPECIAL,
    ETC;

    public static UsageStep of(String value) {
        if (value == null || value.isBlank()) {
            throw new ProductException(ProductErrorCode.INVALID_USAGE_STEP);
        }
        try {
            return UsageStep.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ProductException(ProductErrorCode.INVALID_USAGE_STEP);
        }
    }
}
