package com.pium.application.skinanalysis.exception;

import com.pium.application.exception.ApplicationException;
import com.pium.exception.ErrorCode;

public class SurveyApplicationException extends ApplicationException {

    public SurveyApplicationException(ErrorCode errorCode) {
        super(errorCode);
    }
}

