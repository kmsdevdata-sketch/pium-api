package com.pium.domain.user.exception;

import com.pium.domain.exception.DomainException;
import com.pium.exception.ErrorCode;
import lombok.Getter;

@Getter
public class UserException extends DomainException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}
