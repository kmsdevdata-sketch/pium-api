package com.pium.adapter.exception;

import com.pium.exception.BaseException;
import com.pium.exception.ErrorCode;
public class AdapterException extends BaseException {
    public AdapterException(ErrorCode errorCode) {
        super(errorCode);
    }
}
