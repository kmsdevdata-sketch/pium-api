package com.pium.application.auth.exception;

import com.pium.exception.BaseException;
import com.pium.exception.ErrorCode;

public class AuthApplicationException extends BaseException {

    public AuthApplicationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
