package com.pium.domain.recommendation.engine;

import com.pium.domain.recommendation.enumtype.MetricLevel;
import com.pium.domain.recommendation.model.SkinInterpretation;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;

import java.util.Map;

/**
 * 피부 지표 조합을 추천 전략의 큰 방향으로 변환한다.
 */
public class RoutineIntentPolicy {

    /**
     * 현재 피부 상태에서 가장 우선할 추천 방향을 결정한다.
     */
    public SkinInterpretation.RoutineIntent resolve(Map<SkinMetric, MetricLevel> levels) {
        RecommendationPolicyValidation.validateLevels(levels);

        if (isHigh(levels, SkinMetric.BARRIER)) {
            return SkinInterpretation.RoutineIntent.BARRIER_RECOVERY;
        }
        if (isHigh(levels, SkinMetric.SENSITIVITY)) {
            return SkinInterpretation.RoutineIntent.SOOTHING_FIRST;
        }
        if (atLeastMid(levels, SkinMetric.DRYNESS)) {
            return SkinInterpretation.RoutineIntent.HYDRATION_BALANCE;
        }
        if (isHigh(levels, SkinMetric.OILINESS) || isHigh(levels, SkinMetric.BLEMISH_PRONENESS)) {
            return SkinInterpretation.RoutineIntent.SEBUM_BLEMISH_BALANCE;
        }
        if (isHigh(levels, SkinMetric.PIGMENTATION_TONE)) {
            return SkinInterpretation.RoutineIntent.TONE_CARE;
        }
        if (isHigh(levels, SkinMetric.AGING_SIGNS)) {
            return SkinInterpretation.RoutineIntent.ANTI_AGING_CARE;
        }
        return SkinInterpretation.RoutineIntent.BASIC_BALANCE;
    }

    private boolean isHigh(Map<SkinMetric, MetricLevel> levels, SkinMetric metric) {
        return RecommendationPolicyValidation.level(levels, metric) == MetricLevel.HIGH;
    }

    private boolean atLeastMid(Map<SkinMetric, MetricLevel> levels, SkinMetric metric) {
        MetricLevel level = RecommendationPolicyValidation.level(levels, metric);
        return level == MetricLevel.MID || level == MetricLevel.HIGH;
    }
}
