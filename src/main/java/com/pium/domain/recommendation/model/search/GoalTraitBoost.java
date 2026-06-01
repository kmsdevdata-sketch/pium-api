package com.pium.domain.recommendation.model.search;

import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.recommendation.model.support.RecommendationValidation;

/**
 * 사용자 goal이 추천 정렬에 더하는 benefit trait boost를 표현한다.
 *
 * @param goal 설문 목표 코드
 * @param trait goal과 연결되는 상품 benefit trait
 * @param weight 추천 점수 보정 가중치
 */
public record GoalTraitBoost(
        String goal,
        RecommendationTrait trait,
        TraitPreference.Weight weight
) {

    public GoalTraitBoost {
        goal = RecommendationValidation.normalizeRequired(
                goal,
                RecommendationErrorCode.INVALID_GOAL_TRAIT_BOOST
        );
        if (trait == null || weight == null) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_GOAL_TRAIT_BOOST);
        }
    }
}
