package com.pium.domain.recommendation.model.scoring;

import com.pium.domain.productprofile.enumtype.EvidenceConfidence;
import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.productprofile.enumtype.TraitStrength;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.recommendation.model.interpretation.RiskConstraint;

/**
 * 상품 risk trait가 추천 정책에 적용된 결과를 표현
 */
public record AppliedRisk(
        ProductRiskTrait trait,
        RiskConstraint.Policy policy,
        TraitStrength strength,
        EvidenceConfidence confidence
) {

    public AppliedRisk {
        if (trait == null || policy == null || strength == null || confidence == null) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_RECOMMENDATION_POLICY);
        }
    }
}
