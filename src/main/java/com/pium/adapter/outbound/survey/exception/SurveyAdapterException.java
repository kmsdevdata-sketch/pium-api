package com.pium.adapter.outbound.survey.exception;

import com.pium.adapter.exception.AdapterException;
import com.pium.exception.ErrorCode;

public class SurveyAdapterException extends AdapterException {

    public SurveyAdapterException(ErrorCode errorCode) {
        super(errorCode);
    }
}

