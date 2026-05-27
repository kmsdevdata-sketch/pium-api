package com.pium.domain.product.exception;

import com.pium.domain.exception.DomainException;

public class ProductException extends DomainException {

    public ProductException(ProductErrorCode errorCode) {
        super(errorCode);
    }
}
