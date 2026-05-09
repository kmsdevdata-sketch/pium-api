package com.layerd.domain.exception;

import com.layerd.exception.BaseException;
import com.layerd.exception.ErrorCode;
import lombok.Getter;

@Getter
public class DomainException extends BaseException {
    public DomainException(ErrorCode errorCode) {
        super(errorCode);
    }
}
