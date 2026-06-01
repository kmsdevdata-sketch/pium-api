package com.pium.domain.recommendation.model.scoring;

import com.pium.domain.productprofile.enumtype.EvidenceConfidence;
import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.productprofile.enumtype.TraitStrength;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;

/**
 * 추천 조건과 상품 benefit trait가 매칭된 결과를 표현한다
 */
public record MatchedTrait(
        RecommendationTrait trait,
        TraitStrength strength,
        EvidenceConfidence confidence
) {

    public MatchedTrait {
        if (trait == null || strength == null || confidence == null) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_RECOMMENDATION_POLICY);
        }
    }
}
