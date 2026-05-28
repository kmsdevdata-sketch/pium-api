package com.pium.domain.productprofile.exception;

import com.pium.domain.exception.DomainException;

public class ProductProfileException extends DomainException {

    public ProductProfileException(ProductProfileErrorCode errorCode) {
        super(errorCode);
    }
}
