package com.pium.adapter.outbound.productprofile.exception;

import com.pium.application.exception.ApplicationException;
import com.pium.exception.ErrorCode;

public class ProductProfileAdapterException extends ApplicationException {

    public ProductProfileAdapterException(ErrorCode errorCode) {
        super(errorCode);
    }
}
