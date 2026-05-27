package com.pium.domain.product.vo;

import com.pium.domain.product.exception.ProductErrorCode;
import com.pium.domain.product.exception.ProductException;

import java.util.UUID;

public record ProductId(String value) {

    public ProductId {
        if (value == null || value.isBlank()) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_ID);
        }
    }

    public static ProductId newId() {
        return new ProductId(UUID.randomUUID().toString());
    }

    public static ProductId of(String value) {
        return new ProductId(value);
    }
}
