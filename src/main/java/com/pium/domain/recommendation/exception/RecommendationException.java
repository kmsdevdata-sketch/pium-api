package com.pium.domain.recommendation.exception;

import com.pium.domain.exception.DomainException;

public class RecommendationException extends DomainException {

    public RecommendationException(RecommendationErrorCode errorCode) {
        super(errorCode);
    }
}
