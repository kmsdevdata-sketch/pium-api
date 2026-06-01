package com.pium.domain.recommendation.engine.interpretation;

import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.recommendation.enumtype.MetricLevel;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.recommendation.model.interpretation.GoalConflictNotice;
import com.pium.domain.recommendation.model.interpretation.GoalNeed;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 설문 goal 코드를 추천 benefit trait boost와 충돌 안내로 변환한다.
 */
public class GoalNeedPolicy {

    /**
     * 사용자의 goal이 추천 점수에 더할 benefit boost 목록을 만든다.
     */
    public List<GoalNeed> goalNeeds(List<String> goals, Map<SkinMetric, MetricLevel> levels) {
        RecommendationPolicyValidation.validateLevels(levels);
        validateGoals(goals);

        List<GoalNeed> goalNeeds = new ArrayList<>();

        for (String goal : goals) {
            switch (goal) {
                case "Q11_1" -> {
                    goalNeeds.add(goalNeed(goal, RecommendationTrait.HYDRATION_SUPPORT, GoalNeed.Boost.MEDIUM));
                    goalNeeds.add(goalNeed(goal, RecommendationTrait.BARRIER_SUPPORT, GoalNeed.Boost.LOW));
                }
                case "Q11_2" -> {
                    goalNeeds.add(goalNeed(goal, RecommendationTrait.BLEMISH_CARE_SUPPORT, activeCareBoost(levels)));
                    goalNeeds.add(goalNeed(goal, RecommendationTrait.SEBUM_CONTROL_SUPPORT, GoalNeed.Boost.LOW));
                }
                case "Q11_3" -> {
                    goalNeeds.add(goalNeed(goal, RecommendationTrait.BRIGHTENING_SUPPORT, GoalNeed.Boost.MEDIUM));
                    goalNeeds.add(goalNeed(goal, RecommendationTrait.UV_PROTECTION, GoalNeed.Boost.LOW));
                }
                case "Q11_4" -> {
                    goalNeeds.add(goalNeed(goal, RecommendationTrait.SEBUM_CONTROL_SUPPORT, GoalNeed.Boost.MEDIUM));
                    goalNeeds.add(goalNeed(goal, RecommendationTrait.EXFOLIATION_EFFECT, activeCareBoost(levels)));
                }
                case "Q11_5" -> {
                    goalNeeds.add(goalNeed(goal, RecommendationTrait.ANTI_AGING_SUPPORT, GoalNeed.Boost.MEDIUM));
                    goalNeeds.add(goalNeed(goal, RecommendationTrait.UV_PROTECTION, GoalNeed.Boost.LOW));
                }
                case "Q11_6" -> {
                    goalNeeds.add(goalNeed(goal, RecommendationTrait.SOOTHING_SUPPORT, GoalNeed.Boost.MEDIUM));
                    goalNeeds.add(goalNeed(goal, RecommendationTrait.BARRIER_SUPPORT, GoalNeed.Boost.MEDIUM));
                }
                default -> {
                }
            }
        }

        return goalNeeds.stream().distinct().toList();
    }

    /**
     * goal과 현재 피부 상태가 충돌하는 경우 사용자 안내 근거를 만든다.
     */
    public List<GoalConflictNotice> conflictNotices(List<String> goals, Map<SkinMetric, MetricLevel> levels) {
        RecommendationPolicyValidation.validateLevels(levels);
        validateGoals(goals);

        List<GoalConflictNotice> notices = new ArrayList<>();

        if (isHigh(levels, SkinMetric.BARRIER) && (goals.contains("Q11_2") || goals.contains("Q11_4"))) {
            notices.add(new GoalConflictNotice(
                    goals.contains("Q11_2") ? "Q11_2" : "Q11_4",
                    "BARRIER_HIGH",
                    "트러블/피지 목표는 반영하되, 현재 장벽 부담이 높아 강한 각질·자극 케어는 제한합니다."
            ));
        }

        if (isHigh(levels, SkinMetric.SENSITIVITY) && goals.contains("Q11_3")) {
            notices.add(new GoalConflictNotice(
                    "Q11_3",
                    "SENSITIVITY_HIGH",
                    "톤 케어 목표는 반영하되, 민감 신호가 높아 저자극 톤 케어를 우선합니다."
            ));
        }

        return notices;
    }

    private GoalNeed.Boost activeCareBoost(Map<SkinMetric, MetricLevel> levels) {
        if (isHigh(levels, SkinMetric.BARRIER) || isHigh(levels, SkinMetric.SENSITIVITY)) {
            return GoalNeed.Boost.LOW;
        }
        return GoalNeed.Boost.MEDIUM;
    }

    private boolean isHigh(Map<SkinMetric, MetricLevel> levels, SkinMetric metric) {
        return RecommendationPolicyValidation.level(levels, metric) == MetricLevel.HIGH;
    }

    private GoalNeed goalNeed(String goal, RecommendationTrait trait, GoalNeed.Boost boost) {
        return new GoalNeed(goal, trait, boost);
    }

    private void validateGoals(List<String> goals) {
        if (goals == null || goals.stream().anyMatch(Objects::isNull)) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_RECOMMENDATION_POLICY);
        }
    }
}
