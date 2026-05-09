package com.layerd.domain.user.exception;

import com.layerd.domain.exception.DomainException;
import com.layerd.exception.ErrorCode;
import lombok.Getter;

@Getter
public class UserException extends DomainException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}
