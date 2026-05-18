package com.pium.domain.skinanalysis.exception;

import com.pium.domain.exception.DomainException;
import com.pium.exception.ErrorCode;
import lombok.Getter;

@Getter
public class SkinAnalysisException extends DomainException {

    public SkinAnalysisException(ErrorCode errorCode) {
        super(errorCode);
    }
}
