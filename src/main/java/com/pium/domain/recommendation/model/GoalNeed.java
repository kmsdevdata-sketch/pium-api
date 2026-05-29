package com.pium.domain.recommendation.model;

import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;

/**
 * 사용자가 선택한 개선 목표가 추천 우선순위에 더하는 benefit trait boost를 표현한다.
 *
 * @param goal 설문 목표 코드
 * @param trait 목표와 연결되는 상품 benefit trait
 * @param boost 추천 점수 보정 강도
 */
public record GoalNeed(
        String goal,
        RecommendationTrait trait,
        Boost boost
) {

    public GoalNeed {
        goal = RecommendationValidation.normalizeRequired(
                goal,
                RecommendationErrorCode.INVALID_GOAL_NEED
        );
        if (trait == null || boost == null) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_GOAL_NEED);
        }
    }

    public enum Boost {
        LOW,
        MEDIUM
    }
}
