package com.pium.domain.recommendation.model.support;

import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;

import java.util.List;
import java.util.Objects;

public final class RecommendationValidation {

    private RecommendationValidation() {
    }

    public static String normalizeRequired(String value, RecommendationErrorCode errorCode) {
        if (value == null || value.isBlank()) {
            throw new RecommendationException(errorCode);
        }
        return value.trim();
    }

    public static <T> List<T> copyRequired(List<T> values, RecommendationErrorCode errorCode) {
        if (values == null) {
            throw new RecommendationException(errorCode);
        }
        if (values.stream().anyMatch(Objects::isNull)) {
            throw new RecommendationException(errorCode);
        }
        return List.copyOf(values);
    }
}
