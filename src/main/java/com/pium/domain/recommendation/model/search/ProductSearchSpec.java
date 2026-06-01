package com.pium.domain.recommendation.model.search;

import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.recommendation.model.support.RecommendationValidation;

import java.util.List;

/**
 * ProductProfile 후보를 조회하고 랭킹하기 위한 추천 검색 조건이다.
 *
 * <p>이 모델은 DB 포트가 아니라 추천 도메인의 조건 모델이다.
 * 실제 상품 조회 방식은 application/outbound 계층의 포트와 어댑터가 결정한다.</p>
 *
 * @param requiredTraits 후보가 우선 충족해야 하는 benefit trait
 * @param preferredTraits 정렬에서 가점을 줄 benefit trait
 * @param goalBoostTraits 사용자의 goal이 만든 추가 boost trait
 * @param blockedRiskTraits 후보에서 제외할 risk trait
 * @param penaltyRiskTraits 후보에는 남기되 강하게 감점할 risk trait
 * @param cautionRiskTraits 주의 문구 또는 약한 감점으로 다룰 risk trait
 * @param categoryHints 우선 탐색할 상품 카테고리 힌트
 * @param fallbackPolicy 후보 부족 시 완화 방식
 */
public record ProductSearchSpec(
        List<TraitRequirement> requiredTraits,
        List<TraitPreference> preferredTraits,
        List<GoalTraitBoost> goalBoostTraits,
        List<ProductRiskTrait> blockedRiskTraits,
        List<ProductRiskTrait> penaltyRiskTraits,
        List<ProductRiskTrait> cautionRiskTraits,
        List<ProductCategory> categoryHints,
        FallbackPolicy fallbackPolicy
) {

    public ProductSearchSpec {
        if (fallbackPolicy == null) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_PRODUCT_SEARCH_SPEC);
        }
        requiredTraits = RecommendationValidation.copyRequired(
                requiredTraits,
                RecommendationErrorCode.INVALID_PRODUCT_SEARCH_SPEC
        );
        preferredTraits = RecommendationValidation.copyRequired(
                preferredTraits,
                RecommendationErrorCode.INVALID_PRODUCT_SEARCH_SPEC
        );
        goalBoostTraits = RecommendationValidation.copyRequired(
                goalBoostTraits,
                RecommendationErrorCode.INVALID_PRODUCT_SEARCH_SPEC
        );
        blockedRiskTraits = RecommendationValidation.copyRequired(
                blockedRiskTraits,
                RecommendationErrorCode.INVALID_PRODUCT_SEARCH_SPEC
        ).stream().distinct().toList();
        penaltyRiskTraits = RecommendationValidation.copyRequired(
                penaltyRiskTraits,
                RecommendationErrorCode.INVALID_PRODUCT_SEARCH_SPEC
        ).stream().distinct().toList();
        cautionRiskTraits = RecommendationValidation.copyRequired(
                cautionRiskTraits,
                RecommendationErrorCode.INVALID_PRODUCT_SEARCH_SPEC
        ).stream().distinct().toList();
        categoryHints = RecommendationValidation.copyRequired(
                categoryHints,
                RecommendationErrorCode.INVALID_PRODUCT_SEARCH_SPEC
        ).stream().distinct().toList();
    }

    public enum FallbackPolicy {
        RELAX_REQUIRED_TO_PREFERRED_KEEP_BLOCKED,
        RELAX_PREFERRED_KEEP_BLOCKED
    }
}
