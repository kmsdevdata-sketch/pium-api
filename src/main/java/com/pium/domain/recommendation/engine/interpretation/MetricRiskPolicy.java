package com.pium.domain.recommendation.engine.interpretation;

import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.recommendation.enumtype.MetricLevel;
import com.pium.domain.recommendation.model.interpretation.RiskConstraint;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 피부 지표 레벨을 추천 시 제한하거나 주의해야 할 상품 risk trait로 변환한다.
 */
public class MetricRiskPolicy {

    /**
     * 현재 피부 상태에서 적용할 risk constraint 목록을 만든다.
     */
    public List<RiskConstraint> constraints(Map<SkinMetric, MetricLevel> levels) {
        RecommendationPolicyValidation.validateLevels(levels);

        List<RiskConstraint> constraints = new ArrayList<>();

        if (isHigh(levels, SkinMetric.BARRIER)) {
            constraints.add(risk(ProductRiskTrait.STRONG_EXFOLIATION_EFFECT, RiskConstraint.Policy.HARD_BLOCK, "BARRIER_HIGH"));
            constraints.add(risk(ProductRiskTrait.HIGH_IRRITATION_RISK, RiskConstraint.Policy.HARD_BLOCK, "BARRIER_HIGH"));
            constraints.add(risk(ProductRiskTrait.STRONG_ACTIVE_RISK, RiskConstraint.Policy.SOFT_PENALTY, "BARRIER_HIGH"));
            constraints.add(risk(ProductRiskTrait.FRAGRANCE_OR_ALLERGEN_RISK, RiskConstraint.Policy.CAUTION, "BARRIER_HIGH"));
        }

        if (isHigh(levels, SkinMetric.SENSITIVITY)) {
            constraints.add(risk(ProductRiskTrait.HIGH_IRRITATION_RISK, RiskConstraint.Policy.HARD_BLOCK, "SENSITIVITY_HIGH"));
            constraints.add(risk(ProductRiskTrait.FRAGRANCE_OR_ALLERGEN_RISK, RiskConstraint.Policy.SOFT_PENALTY, "SENSITIVITY_HIGH"));
            constraints.add(risk(ProductRiskTrait.IRRITATION_RISK, RiskConstraint.Policy.CAUTION, "SENSITIVITY_HIGH"));
            constraints.add(risk(ProductRiskTrait.STRONG_ACTIVE_RISK, RiskConstraint.Policy.SOFT_PENALTY, "SENSITIVITY_HIGH"));
        }

        if (isMid(levels, SkinMetric.SENSITIVITY)) {
            constraints.add(risk(ProductRiskTrait.FRAGRANCE_OR_ALLERGEN_RISK, RiskConstraint.Policy.CAUTION, "SENSITIVITY_MID"));
        }

        if (isHigh(levels, SkinMetric.DRYNESS) || isHigh(levels, SkinMetric.BARRIER)) {
            constraints.add(risk(ProductRiskTrait.DRYING_OR_STRIPPING_RISK, RiskConstraint.Policy.SOFT_PENALTY, "DRYNESS_OR_BARRIER_HIGH"));
        }

        if (isHigh(levels, SkinMetric.OILINESS) || isHigh(levels, SkinMetric.BLEMISH_PRONENESS)) {
            constraints.add(risk(ProductRiskTrait.HEAVY_OCCLUSIVE_RISK, RiskConstraint.Policy.CAUTION, "OILINESS_OR_BLEMISH_HIGH"));
            constraints.add(risk(ProductRiskTrait.COMEDOGENIC_RISK, RiskConstraint.Policy.CAUTION, "OILINESS_OR_BLEMISH_HIGH"));
        }

        return constraints.stream().distinct().toList();
    }

    private boolean isHigh(Map<SkinMetric, MetricLevel> levels, SkinMetric metric) {
        return RecommendationPolicyValidation.level(levels, metric) == MetricLevel.HIGH;
    }

    private boolean isMid(Map<SkinMetric, MetricLevel> levels, SkinMetric metric) {
        return RecommendationPolicyValidation.level(levels, metric) == MetricLevel.MID;
    }

    private RiskConstraint risk(ProductRiskTrait trait, RiskConstraint.Policy policy, String source) {
        return new RiskConstraint(trait, policy, source);
    }
}
