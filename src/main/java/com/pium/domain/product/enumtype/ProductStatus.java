package com.pium.domain.product.enumtype;

import com.pium.domain.product.exception.ProductErrorCode;
import com.pium.domain.product.exception.ProductException;

public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    EXCLUDED;

    public static ProductStatus of(String value) {
        if (value == null || value.isBlank()) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_STATUS);
        }
        try {
            return ProductStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_STATUS);
        }
    }
}
