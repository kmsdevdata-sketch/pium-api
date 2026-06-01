package com.pium.application.recommendation.exception;

import com.pium.application.exception.ApplicationException;
import com.pium.exception.ErrorCode;

public class RecommendationApplicationException extends ApplicationException {

    public RecommendationApplicationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
