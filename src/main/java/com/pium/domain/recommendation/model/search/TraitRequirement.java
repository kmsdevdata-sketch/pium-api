package com.pium.domain.recommendation.model.search;

import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.productprofile.enumtype.TraitStrength;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;

/**
 * 추천 후보가 우선 충족해야 하는 benefit trait 조건을 표현한다.
 *
 * @param trait 필요한 상품 benefit trait
 * @param minStrength 후보로 인정할 최소 trait 강도
 */
public record TraitRequirement(
        RecommendationTrait trait,
        TraitStrength minStrength
) {

    public TraitRequirement {
        if (trait == null || minStrength == null) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_TRAIT_REQUIREMENT);
        }
    }
}
