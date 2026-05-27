package com.pium.domain.product.enumtype;

import com.pium.domain.product.exception.ProductErrorCode;
import com.pium.domain.product.exception.ProductException;

public enum ProductCategory {
    CLEANSER,
    TONER,
    ESSENCE_SERUM,
    LOTION_CREAM,
    SUN_CARE,
    MASK_PACK,
    EXFOLIATOR,
    SPOT_CARE,
    MIST,
    OIL_BALM,
    ETC;

    public static ProductCategory of(String value) {
        if (value == null || value.isBlank()) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_CATEGORY);
        }
        try {
            return ProductCategory.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_CATEGORY);
        }
    }
}
