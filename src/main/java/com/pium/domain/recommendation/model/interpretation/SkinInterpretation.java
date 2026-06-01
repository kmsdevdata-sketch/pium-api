package com.pium.domain.recommendation.model.interpretation;

import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.recommendation.model.support.RecommendationValidation;

import java.util.List;

/**
 * 피부 분석 결과를 추천 엔진이 읽을 수 있는 중간 의미 모델로 변환한 결과다.
 *
 * <p>이 모델은 상품을 직접 조회하거나 점수화하지 않는다.
 * 현재 피부 상태가 요구하는 benefit, 조심해야 할 risk, goal boost, 전체 루틴 방향만 표현한다.</p>
 *
 * @param resultId 해석 대상 피부 분석 결과 ID
 * @param routineIntent 현재 피부 상태에서 우선할 추천 방향
 * @param primaryNeeds 우선 충족해야 하는 benefit trait
 * @param secondaryNeeds 있으면 좋은 보조 benefit trait
 * @param riskConstraints 주의하거나 제한해야 하는 product risk trait
 * @param goalNeeds 사용자의 goal이 만든 추천 boost
 * @param goalConflictNotices goal과 피부 상태 충돌 안내
 */
public record SkinInterpretation(
        String resultId,
        RoutineIntent routineIntent,
        List<SkinNeed> primaryNeeds,
        List<SkinNeed> secondaryNeeds,
        List<RiskConstraint> riskConstraints,
        List<GoalNeed> goalNeeds,
        List<GoalConflictNotice> goalConflictNotices
) {

    public SkinInterpretation {
        resultId = RecommendationValidation.normalizeRequired(
                resultId,
                RecommendationErrorCode.INVALID_SKIN_INTERPRETATION
        );
        if (routineIntent == null) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_SKIN_INTERPRETATION);
        }
        primaryNeeds = RecommendationValidation.copyRequired(
                primaryNeeds,
                RecommendationErrorCode.INVALID_SKIN_INTERPRETATION
        );
        secondaryNeeds = RecommendationValidation.copyRequired(
                secondaryNeeds,
                RecommendationErrorCode.INVALID_SKIN_INTERPRETATION
        );
        riskConstraints = RecommendationValidation.copyRequired(
                riskConstraints,
                RecommendationErrorCode.INVALID_SKIN_INTERPRETATION
        );
        goalNeeds = RecommendationValidation.copyRequired(
                goalNeeds,
                RecommendationErrorCode.INVALID_SKIN_INTERPRETATION
        );
        goalConflictNotices = RecommendationValidation.copyRequired(
                goalConflictNotices,
                RecommendationErrorCode.INVALID_SKIN_INTERPRETATION
        );
    }

    public enum RoutineIntent {
        BARRIER_RECOVERY,
        SOOTHING_FIRST,
        HYDRATION_BALANCE,
        SEBUM_BLEMISH_BALANCE,
        TONE_CARE,
        ANTI_AGING_CARE,
        BASIC_BALANCE
    }
}
