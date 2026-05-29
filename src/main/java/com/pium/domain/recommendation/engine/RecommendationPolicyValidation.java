package com.pium.domain.recommendation.engine;

import com.pium.domain.recommendation.enumtype.MetricLevel;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;

import java.util.Arrays;
import java.util.Map;

final class RecommendationPolicyValidation {

    private RecommendationPolicyValidation() {
    }

    static void validateLevels(Map<SkinMetric, MetricLevel> levels) {
        if (levels == null) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_SKIN_METRIC_LEVELS);
        }

        boolean hasMissingMetric = Arrays.stream(SkinMetric.values())
                .anyMatch(metric -> levels.get(metric) == null);

        if (hasMissingMetric) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_SKIN_METRIC_LEVELS);
        }
    }

    static MetricLevel level(Map<SkinMetric, MetricLevel> levels, SkinMetric metric) {
        validateLevels(levels);
        return levels.get(metric);
    }
}
