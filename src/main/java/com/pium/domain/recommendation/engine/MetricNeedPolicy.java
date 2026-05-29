package com.pium.domain.recommendation.engine;

import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.recommendation.enumtype.MetricLevel;
import com.pium.domain.recommendation.model.SkinNeed;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 피부 지표 레벨을 필요한 상품 benefit trait로 변환한다.
 */
public class MetricNeedPolicy {

    /**
     * HIGH 상태 지표에서 우선 need를 만든다.
     */
    public List<SkinNeed> primaryNeeds(Map<SkinMetric, MetricLevel> levels) {
        RecommendationPolicyValidation.validateLevels(levels);

        List<SkinNeed> needs = new ArrayList<>();
        addIfHigh(needs, levels, SkinMetric.DRYNESS, RecommendationTrait.HYDRATION_SUPPORT);
        addIfHigh(needs, levels, SkinMetric.BARRIER, RecommendationTrait.BARRIER_SUPPORT);
        addIfHigh(needs, levels, SkinMetric.SENSITIVITY, RecommendationTrait.SOOTHING_SUPPORT);
        addIfHigh(needs, levels, SkinMetric.OILINESS, RecommendationTrait.SEBUM_CONTROL_SUPPORT);
        addIfHigh(needs, levels, SkinMetric.BLEMISH_PRONENESS, RecommendationTrait.BLEMISH_CARE_SUPPORT);
        addIfHigh(needs, levels, SkinMetric.PIGMENTATION_TONE, RecommendationTrait.BRIGHTENING_SUPPORT);
        addIfHigh(needs, levels, SkinMetric.AGING_SIGNS, RecommendationTrait.ANTI_AGING_SUPPORT);
        return needs.stream().distinct().toList();
    }

    /**
     * MID 상태 지표에서 보조 need를 만든다.
     */
    public List<SkinNeed> secondaryNeeds(Map<SkinMetric, MetricLevel> levels) {
        RecommendationPolicyValidation.validateLevels(levels);

        List<SkinNeed> needs = new ArrayList<>();
        addIfMid(needs, levels, SkinMetric.DRYNESS, RecommendationTrait.HYDRATION_SUPPORT);
        addIfMid(needs, levels, SkinMetric.BARRIER, RecommendationTrait.BARRIER_SUPPORT);
        addIfMid(needs, levels, SkinMetric.SENSITIVITY, RecommendationTrait.SOOTHING_SUPPORT);
        addIfMid(needs, levels, SkinMetric.OILINESS, RecommendationTrait.SEBUM_CONTROL_SUPPORT);
        addIfMid(needs, levels, SkinMetric.BLEMISH_PRONENESS, RecommendationTrait.BLEMISH_CARE_SUPPORT);
        addIfMid(needs, levels, SkinMetric.PIGMENTATION_TONE, RecommendationTrait.BRIGHTENING_SUPPORT);
        addIfMid(needs, levels, SkinMetric.AGING_SIGNS, RecommendationTrait.ANTI_AGING_SUPPORT);
        return needs.stream().distinct().toList();
    }

    private void addIfHigh(
            List<SkinNeed> needs,
            Map<SkinMetric, MetricLevel> levels,
            SkinMetric metric,
            RecommendationTrait trait
    ) {
        if (levels.get(metric) == MetricLevel.HIGH) {
            needs.add(new SkinNeed(trait, SkinNeed.Intensity.REQUIRED, metric.name() + "_HIGH"));
        }
    }

    private void addIfMid(
            List<SkinNeed> needs,
            Map<SkinMetric, MetricLevel> levels,
            SkinMetric metric,
            RecommendationTrait trait
    ) {
        if (levels.get(metric) == MetricLevel.MID) {
            needs.add(new SkinNeed(trait, SkinNeed.Intensity.PREFERRED, metric.name() + "_MID"));
        }
    }
}
