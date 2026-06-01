package com.pium.domain.recommendation.engine.search;

import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.productprofile.enumtype.TraitStrength;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.recommendation.model.interpretation.GoalNeed;
import com.pium.domain.recommendation.model.search.GoalTraitBoost;
import com.pium.domain.recommendation.model.search.ProductSearchSpec;
import com.pium.domain.recommendation.model.interpretation.RiskConstraint;
import com.pium.domain.recommendation.model.interpretation.SkinInterpretation;
import com.pium.domain.recommendation.model.interpretation.SkinNeed;
import com.pium.domain.recommendation.model.search.TraitPreference;
import com.pium.domain.recommendation.model.search.TraitRequirement;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * SkinInterpretation을 ProductProfile 후보 조회와 랭킹에 사용할 ProductSearchSpec으로 변환한다.
 */
public class ProductSearchSpecGenerator {

    /**
     * 피부 해석 결과를 상품 검색 조건으로 변환한다.
     */
    public ProductSearchSpec generate(SkinInterpretation interpretation) {
        if (Objects.isNull(interpretation)) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_RECOMMENDATION_POLICY);
        }

        Map<ProductRiskTrait, RiskConstraint.Policy> riskPolicies = strongestRiskPolicies(
                interpretation.riskConstraints()
        );

        return new ProductSearchSpec(
                requiredTraits(interpretation),
                preferredTraits(interpretation),
                goalBoostTraits(interpretation.goalNeeds()),
                riskTraitsByPolicy(riskPolicies, RiskConstraint.Policy.HARD_BLOCK),
                riskTraitsByPolicy(riskPolicies, RiskConstraint.Policy.SOFT_PENALTY),
                riskTraitsByPolicy(riskPolicies, RiskConstraint.Policy.CAUTION),
                categoryHints(interpretation.routineIntent()),
                fallbackPolicy(interpretation)
        );
    }

    /**
     * primaryNeed중 REQUIRED(필수)인 benefit trait를 뽑는다
     */
    private List<TraitRequirement> requiredTraits(SkinInterpretation interpretation) {
        return interpretation.primaryNeeds().stream()
                .filter(need -> need.intensity() == SkinNeed.Intensity.REQUIRED)
                .map(need -> new TraitRequirement(need.trait(), TraitStrength.WEAK))
                .distinct()
                .toList();
    }

    /**
     * secondaryNeeds와 primaryNeeds 중 PREFERRED(우선)가 있으면 선호 trait로 바꾼다
     * <p>=> 있으면 점수를 더주자 - required보다 약한 조건
     */
    private List<TraitPreference> preferredTraits(SkinInterpretation interpretation) {
        return Stream.concat(
                        interpretation.primaryNeeds().stream()
                                .filter(need -> need.intensity() == SkinNeed.Intensity.PREFERRED)
                                .map(SkinNeed::trait),
                        interpretation.secondaryNeeds().stream()
                                .map(SkinNeed::trait)
                )
                .map(trait -> new TraitPreference(trait, TraitPreference.Weight.MEDIUM))
                .distinct()
                .toList();
    }

    /**
     * 사용자가 선택한 gaol을 점수 boost조건으로 바꾼다
     * <p>피부 상태가 허용하는 범위 안에서 사용자의 목표 방향 상품을 조금더 올리기
     * <p>=> goal은 safety를 뒤집지 않는다
     */
    private List<GoalTraitBoost> goalBoostTraits(List<GoalNeed> goalNeeds) {
        return goalNeeds.stream()
                .map(goalNeed -> new GoalTraitBoost(
                        goalNeed.goal(),
                        goalNeed.trait(),
                        weightOf(goalNeed.boost())
                ))
                .distinct()
                .toList();
    }

    private TraitPreference.Weight weightOf(GoalNeed.Boost boost) {
        return switch (boost) {
            case LOW -> TraitPreference.Weight.LOW;
            case MEDIUM -> TraitPreference.Weight.MEDIUM;
        };
    }

    private Map<ProductRiskTrait, RiskConstraint.Policy> strongestRiskPolicies(
            List<RiskConstraint> riskConstraints
    ) {
        Map<ProductRiskTrait, RiskConstraint.Policy> policies = new EnumMap<>(ProductRiskTrait.class);
        for (RiskConstraint constraint : riskConstraints) {
            policies.merge(
                    constraint.trait(),
                    constraint.policy(),
                    this::strongerPolicy
            );
        }
        return policies;
    }

    private RiskConstraint.Policy strongerPolicy(
            RiskConstraint.Policy existingPolicy,
            RiskConstraint.Policy newPolicy
    ) {
        if (priorityOf(newPolicy) > priorityOf(existingPolicy)) {
            return newPolicy;
        }
        return existingPolicy;
    }

    private int priorityOf(RiskConstraint.Policy policy) {
        return switch (policy) {
            case HARD_BLOCK -> 3;
            case SOFT_PENALTY -> 2;
            case CAUTION -> 1;
        };
    }

    /**
     * 현재 피부 상태에서 추천 후보에서 제외해야할 risk trait를 뽑는다
     * <p> policy = HARD_BLOCK : 후보에서 제외
     * <p> policy = SOFT_PENALTY : 후보에는 남기되 강하게 감점
     * <p> policy = CAUTION : 추천은 가능하지만 주의문구 or 약한 감점
     */
    private List<ProductRiskTrait> riskTraitsByPolicy(
            Map<ProductRiskTrait, RiskConstraint.Policy> policies,
            RiskConstraint.Policy policy
    ) {
        return policies.entrySet().stream()
                .filter(entry -> entry.getValue() == policy)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     *  추천 방향에 맞춰 우선 볼 상품 카테고리 힌트를 만든다
     *  먼저 보기 좋은 카테고리
     */
    private List<ProductCategory> categoryHints(SkinInterpretation.RoutineIntent intent) {
        return switch (intent) {
            case BARRIER_RECOVERY, HYDRATION_BALANCE -> List.of(
                    ProductCategory.TONER,
                    ProductCategory.ESSENCE_SERUM,
                    ProductCategory.LOTION_CREAM,
                    ProductCategory.MIST
            );
            case SOOTHING_FIRST -> List.of(
                    ProductCategory.TONER,
                    ProductCategory.ESSENCE_SERUM,
                    ProductCategory.LOTION_CREAM,
                    ProductCategory.MASK_PACK
            );
            case SEBUM_BLEMISH_BALANCE -> List.of(
                    ProductCategory.TONER,
                    ProductCategory.ESSENCE_SERUM,
                    ProductCategory.SPOT_CARE,
                    ProductCategory.LOTION_CREAM
            );
            case TONE_CARE, ANTI_AGING_CARE -> List.of(
                    ProductCategory.ESSENCE_SERUM,
                    ProductCategory.SUN_CARE,
                    ProductCategory.LOTION_CREAM
            );
            case BASIC_BALANCE -> List.of(
                    ProductCategory.TONER,
                    ProductCategory.ESSENCE_SERUM,
                    ProductCategory.LOTION_CREAM,
                    ProductCategory.SUN_CARE
            );
        };
    }

    /**
     * 추천 후보가 부족할 떄 조건을 어떻게 완화할지 정한다
     */
    private ProductSearchSpec.FallbackPolicy fallbackPolicy(SkinInterpretation interpretation) {
        if (interpretation.primaryNeeds().isEmpty()) {
            return ProductSearchSpec.FallbackPolicy.RELAX_PREFERRED_KEEP_BLOCKED;
        }
        return ProductSearchSpec.FallbackPolicy.RELAX_REQUIRED_TO_PREFERRED_KEEP_BLOCKED;
    }
}
