package com.pium.domain.recommendation.engine.scoring;

import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.productprofile.model.ProductProfile;
import com.pium.domain.productprofile.model.ProductTraitSignal;
import com.pium.domain.recommendation.enumtype.ScoreBand;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.recommendation.model.scoring.AppliedRisk;
import com.pium.domain.recommendation.model.search.GoalTraitBoost;
import com.pium.domain.recommendation.model.scoring.MatchedTrait;
import com.pium.domain.recommendation.model.search.ProductSearchSpec;
import com.pium.domain.recommendation.model.interpretation.RiskConstraint;
import com.pium.domain.recommendation.model.scoring.ScoredRecommendationCandidate;
import com.pium.domain.recommendation.model.search.TraitPreference;
import com.pium.domain.recommendation.model.search.TraitRequirement;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ProductSearchSpec과 ProductProfile을 비교해 추천 후보 점수와 적용된 근거를 계산한다.
 *
 * <p>점수 상수의 의도와 운영 중 조정 기준은 docs/recommendation/RecommendationScoringPolicy.md를 기준으로 한다.</p>
 */
public class RecommendationPolicy {

    private static final int REQUIRED_TRAIT_SCORE = 40;
    private static final int PREFERRED_TRAIT_SCORE = 20;
    private static final int GOAL_MEDIUM_SCORE = 15;
    private static final int GOAL_LOW_SCORE = 8;
    private static final int PENALTY_RISK_SCORE = -25;
    private static final int CAUTION_RISK_SCORE = -8;
    private static final int CATEGORY_HINT_SCORE = 5;

    /**
     * 상품 프로파일 후보 목록을 점수화하고 추천 가능한 후보만 반환
     */
    public List<ScoredRecommendationCandidate> score(
            ProductSearchSpec spec,
            List<ProductProfile> productProfiles
    ) {
        if (spec == null || productProfiles == null || productProfiles.stream().anyMatch(Objects::isNull)) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_RECOMMENDATION_POLICY);
        }

        return productProfiles.stream()
                .map(profile -> score(spec, profile))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingInt(ScoredRecommendationCandidate::score).reversed())
                .toList();
    }

    /**
     * 상품 프로파일 하나를 점수화
     *
     * <p>blocked risk trait가 있으면 후보에서 제외하기 위해 Optional.empty()를 반환한다.</p>
     */
    public Optional<ScoredRecommendationCandidate> score(ProductSearchSpec spec, ProductProfile profile) {
        if (spec == null || profile == null) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_RECOMMENDATION_POLICY);
        }

        if (hasBlockedRisk(spec, profile)) {
            return Optional.empty();
        }

        List<MatchedTrait> matchedRequiredTraits = matchedRequiredTraits(spec, profile);
        List<MatchedTrait> matchedPreferredTraits = matchedPreferredTraits(spec, profile);
        List<MatchedTrait> matchedGoalTraits = matchedGoalTraits(spec, profile);
        List<AppliedRisk> penaltyRisks = appliedRisks(
                spec.penaltyRiskTraits(),
                RiskConstraint.Policy.SOFT_PENALTY,
                profile
        );
        List<AppliedRisk> cautionRisks = appliedRisks(
                spec.cautionRiskTraits(),
                RiskConstraint.Policy.CAUTION,
                profile
        );

        int score = 0;
        score += matchedRequiredTraits.size() * REQUIRED_TRAIT_SCORE;
        score += matchedPreferredTraits.size() * PREFERRED_TRAIT_SCORE;
        score += goalScore(spec, matchedGoalTraits);
        score += penaltyRisks.size() * PENALTY_RISK_SCORE;
        score += cautionRisks.size() * CAUTION_RISK_SCORE;
        score += categoryHintScore(spec, profile);

        return Optional.of(new ScoredRecommendationCandidate(
                profile.productId(),
                Math.max(score, 0),
                scoreBand(score),
                matchedRequiredTraits,
                matchedPreferredTraits,
                matchedGoalTraits,
                penaltyRisks,
                cautionRisks
        ));
    }

    private boolean hasBlockedRisk(ProductSearchSpec spec, ProductProfile profile) {
        return profile.riskTraits().stream()
                .anyMatch(signal -> spec.blockedRiskTraits().contains(signal.trait()));
    }

    private List<MatchedTrait> matchedRequiredTraits(ProductSearchSpec spec, ProductProfile profile) {
        return spec.requiredTraits().stream()
                .map(requirement -> findBenefitTrait(requirement, profile))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<MatchedTrait> findBenefitTrait(TraitRequirement requirement, ProductProfile profile) {
        return profile.benefitTraits().stream()
                .filter(signal -> signal.trait() == requirement.trait())
                .filter(signal -> signal.strength().ordinal() >= requirement.minStrength().ordinal())
                .map(this::matchedTrait)
                .findFirst();
    }

    private List<MatchedTrait> matchedPreferredTraits(ProductSearchSpec spec, ProductProfile profile) {
        return spec.preferredTraits().stream()
                .map(preference -> findBenefitTrait(preference.trait(), profile))
                .flatMap(Optional::stream)
                .toList();
    }

    private List<MatchedTrait> matchedGoalTraits(ProductSearchSpec spec, ProductProfile profile) {
        return spec.goalBoostTraits().stream()
                .map(boost -> findBenefitTrait(boost.trait(), profile))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<MatchedTrait> findBenefitTrait(RecommendationTrait trait, ProductProfile profile) {
        return profile.benefitTraits().stream()
                .filter(signal -> signal.trait() == trait)
                .map(this::matchedTrait)
                .findFirst();
    }

    private MatchedTrait matchedTrait(ProductTraitSignal<RecommendationTrait> signal) {
        return new MatchedTrait(signal.trait(), signal.strength(), signal.confidence());
    }

    private List<AppliedRisk> appliedRisks(
            List<ProductRiskTrait> riskTraits,
            RiskConstraint.Policy policy,
            ProductProfile profile
    ) {
        return profile.riskTraits().stream()
                .filter(signal -> riskTraits.contains(signal.trait()))
                .map(signal -> new AppliedRisk(
                        signal.trait(),
                        policy,
                        signal.strength(),
                        signal.confidence()
                ))
                .toList();
    }

    private int goalScore(ProductSearchSpec spec, List<MatchedTrait> matchedGoalTraits) {
        int score = 0;
        for (GoalTraitBoost boost : spec.goalBoostTraits()) {
            boolean matched = matchedGoalTraits.stream()
                    .anyMatch(trait -> trait.trait() == boost.trait());

            if (matched) {
                score += scoreOf(boost.weight());
            }
        }
        return score;
    }

    private int scoreOf(TraitPreference.Weight weight) {
        return switch (weight) {
            case LOW -> GOAL_LOW_SCORE;
            case MEDIUM -> GOAL_MEDIUM_SCORE;
        };
    }

    private int categoryHintScore(ProductSearchSpec spec, ProductProfile profile) {
        if (spec.categoryHints().contains(profile.category())) {
            return CATEGORY_HINT_SCORE;
        }
        return 0;
    }

    private ScoreBand scoreBand(int score) {
        int normalizedScore = Math.max(score, 0);
        if (normalizedScore >= 70) {
            return ScoreBand.HIGH;
        }
        if (normalizedScore >= 40) {
            return ScoreBand.MEDIUM;
        }
        return ScoreBand.LOW;
    }
}
