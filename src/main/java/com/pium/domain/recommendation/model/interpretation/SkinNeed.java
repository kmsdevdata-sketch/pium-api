package com.pium.domain.recommendation.model.interpretation;

import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.recommendation.model.support.RecommendationValidation;

/**
 * 현재 피부 상태가 필요로 하는 상품 benefit trait를 표현한다.
 *
 * @param trait 필요한 상품 benefit trait
 * @param intensity 필수/선호 수준
 * @param source 이 need가 생성된 피부 상태 근거
 */
public record SkinNeed(
        RecommendationTrait trait,
        Intensity intensity,
        String source
) {

    public SkinNeed {
        if (trait == null || intensity == null) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_SKIN_NEED);
        }
        source = RecommendationValidation.normalizeRequired(
                source,
                RecommendationErrorCode.INVALID_SKIN_NEED
        );
    }

    public enum Intensity {
        REQUIRED,
        PREFERRED
    }
}
