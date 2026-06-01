package com.pium.domain.recommendation.model.scoring;

import com.pium.domain.product.vo.ProductId;
import com.pium.domain.recommendation.enumtype.ScoreBand;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.recommendation.model.support.RecommendationValidation;

import java.util.List;

/**
 * ProductProfile 하나를 추천 조건과 비교해 점수화한 결과
 *
 * <p>이 모델은 아직 사용자 응답 DTO가 아니다.
 * 이후 application 계층에서 Product 원본 정보와 결합해 RecommendationResult 응답으로 변환</p>
 *
 * @param productId 추천 후보 상품 ID
 * @param score 추천 정렬 점수
 * @param scoreBand 추천 점수 구간
 * @param matchedRequiredTraits required 조건과 매칭된 benefit trait
 * @param matchedPreferredTraits preferred 조건과 매칭된 benefit trait
 * @param matchedGoalTraits goal boost와 매칭된 benefit trait
 * @param penaltyRisks 감점으로 적용된 risk trait
 * @param cautionRisks 주의로 적용된 risk trait
 */
public record ScoredRecommendationCandidate(
        ProductId productId,
        int score,
        ScoreBand scoreBand,
        List<MatchedTrait> matchedRequiredTraits,
        List<MatchedTrait> matchedPreferredTraits,
        List<MatchedTrait> matchedGoalTraits,
        List<AppliedRisk> penaltyRisks,
        List<AppliedRisk> cautionRisks
) {

    public ScoredRecommendationCandidate {
        if (productId == null || scoreBand == null) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_RECOMMENDATION_POLICY);
        }
        matchedRequiredTraits = RecommendationValidation.copyRequired(
                matchedRequiredTraits,
                RecommendationErrorCode.INVALID_RECOMMENDATION_POLICY
        );
        matchedPreferredTraits = RecommendationValidation.copyRequired(
                matchedPreferredTraits,
                RecommendationErrorCode.INVALID_RECOMMENDATION_POLICY
        );
        matchedGoalTraits = RecommendationValidation.copyRequired(
                matchedGoalTraits,
                RecommendationErrorCode.INVALID_RECOMMENDATION_POLICY
        );
        penaltyRisks = RecommendationValidation.copyRequired(
                penaltyRisks,
                RecommendationErrorCode.INVALID_RECOMMENDATION_POLICY
        );
        cautionRisks = RecommendationValidation.copyRequired(
                cautionRisks,
                RecommendationErrorCode.INVALID_RECOMMENDATION_POLICY
        );
    }
}
