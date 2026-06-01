package com.pium.domain.recommendation.model.interpretation;

import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.recommendation.model.support.RecommendationValidation;

/**
 * 현재 피부 상태에서 조심해야 하는 상품 risk trait와 적용 정책을 표현한다.
 *
 * @param trait 주의할 상품 risk trait
 * @param policy 추천 정책에서 적용할 제한 수준
 * @param source 이 제약이 생성된 피부 상태 근거
 */
public record RiskConstraint(
        ProductRiskTrait trait,
        Policy policy,
        String source
) {

    public RiskConstraint {
        if (trait == null || policy == null) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_RISK_CONSTRAINT);
        }
        source = RecommendationValidation.normalizeRequired(
                source,
                RecommendationErrorCode.INVALID_RISK_CONSTRAINT
        );
    }

    public enum Policy {
        HARD_BLOCK,
        SOFT_PENALTY,
        CAUTION
    }
}
