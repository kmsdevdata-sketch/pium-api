package com.pium.domain.recommendation.model.search;

import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;

/**
 * 추천 후보 정렬에서 가점을 줄 benefit trait 조건을 표현한다.
 *
 * @param trait 선호하는 상품 benefit trait
 * @param weight 추천 점수 가중치
 */
public record TraitPreference(
        RecommendationTrait trait,
        Weight weight
) {

    public TraitPreference {
        if (trait == null || weight == null) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_TRAIT_PREFERENCE);
        }
    }

    public enum Weight {
        LOW,
        MEDIUM
    }
}
