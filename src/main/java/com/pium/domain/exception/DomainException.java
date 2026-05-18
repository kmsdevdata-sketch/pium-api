package com.pium.domain.exception;

import com.pium.exception.BaseException;
import com.pium.exception.ErrorCode;
import lombok.Getter;

@Getter
public class DomainException extends BaseException {
    public DomainException(ErrorCode errorCode) {
        super(errorCode);
    }
}
