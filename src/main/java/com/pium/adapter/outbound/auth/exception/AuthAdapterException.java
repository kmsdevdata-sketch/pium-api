package com.pium.adapter.outbound.auth.exception;

import com.pium.application.exception.ApplicationException;
import com.pium.exception.ErrorCode;

public class AuthAdapterException extends ApplicationException {
    public AuthAdapterException(ErrorCode errorCode){super(errorCode);}
}
