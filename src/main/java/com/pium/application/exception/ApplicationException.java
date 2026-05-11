package com.pium.application.exception;

import com.pium.exception.BaseException;
import com.pium.exception.ErrorCode;
public class ApplicationException extends BaseException {
    public ApplicationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
