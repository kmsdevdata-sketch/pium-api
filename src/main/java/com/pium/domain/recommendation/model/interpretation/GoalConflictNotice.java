package com.pium.domain.recommendation.model.interpretation;

import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.model.support.RecommendationValidation;

/**
 * 사용자의 goal과 현재 피부 상태가 충돌할 때 추천 설명에 사용할 안내 근거를 표현한다.
 *
 * @param goal 충돌이 발생한 목표 코드
 * @param source 충돌 판단의 피부 상태 근거
 * @param message 사용자에게 설명할 수 있는 안내 문장
 */
public record GoalConflictNotice(
        String goal,
        String source,
        String message
) {

    public GoalConflictNotice {
        goal = RecommendationValidation.normalizeRequired(
                goal,
                RecommendationErrorCode.INVALID_GOAL_CONFLICT_NOTICE
        );
        source = RecommendationValidation.normalizeRequired(
                source,
                RecommendationErrorCode.INVALID_GOAL_CONFLICT_NOTICE
        );
        message = RecommendationValidation.normalizeRequired(
                message,
                RecommendationErrorCode.INVALID_GOAL_CONFLICT_NOTICE
        );
    }
}
