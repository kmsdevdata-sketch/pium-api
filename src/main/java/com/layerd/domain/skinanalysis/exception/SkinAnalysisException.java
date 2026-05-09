package com.layerd.domain.skinanalysis.exception;

import com.layerd.domain.exception.DomainException;
import com.layerd.exception.ErrorCode;
import lombok.Getter;

@Getter
public class SkinAnalysisException extends DomainException {

    public SkinAnalysisException(ErrorCode errorCode) {
        super(errorCode);
    }
}
