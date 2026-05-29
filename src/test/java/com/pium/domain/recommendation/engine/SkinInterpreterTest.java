package com.pium.domain.recommendation.engine;

import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.recommendation.model.GoalNeed;
import com.pium.domain.recommendation.model.RiskConstraint;
import com.pium.domain.recommendation.model.SkinInterpretation;
import com.pium.domain.recommendation.model.SkinNeed;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkinInterpreterTest {

    private final SkinInterpreter skinInterpreter = new SkinInterpreter();

    @Test
    void interpret_피부분석결과를_추천_중간해석으로_변환한다() {
        SkinAnalysisResult result = SkinAnalysisResult.create(
                UserId.of("user-1"),
                List.of(
                        SkinMetricScore.of(SkinMetric.DRYNESS, 82),
                        SkinMetricScore.of(SkinMetric.BARRIER, 45),
                        SkinMetricScore.of(SkinMetric.OILINESS, 20),
                        SkinMetricScore.of(SkinMetric.BLEMISH_PRONENESS, 30),
                        SkinMetricScore.of(SkinMetric.SENSITIVITY, 76),
                        SkinMetricScore.of(SkinMetric.PIGMENTATION_TONE, 58),
                        SkinMetricScore.of(SkinMetric.AGING_SIGNS, 10)
                ),
                List.of("Q11_3")
        );

        SkinInterpretation interpretation = skinInterpreter.interpret(result);

        assertThat(interpretation.resultId()).isEqualTo(result.getId().value());
        assertThat(interpretation.routineIntent()).isEqualTo(SkinInterpretation.RoutineIntent.SOOTHING_FIRST);
        assertThat(interpretation.primaryNeeds())
                .extracting(SkinNeed::trait)
                .contains(RecommendationTrait.HYDRATION_SUPPORT, RecommendationTrait.SOOTHING_SUPPORT);
        assertThat(interpretation.secondaryNeeds())
                .extracting(SkinNeed::trait)
                .contains(RecommendationTrait.BARRIER_SUPPORT, RecommendationTrait.BRIGHTENING_SUPPORT);
        assertThat(interpretation.riskConstraints())
                .extracting(RiskConstraint::trait)
                .contains(
                        ProductRiskTrait.FRAGRANCE_OR_ALLERGEN_RISK,
                        ProductRiskTrait.IRRITATION_RISK,
                        ProductRiskTrait.STRONG_ACTIVE_RISK
                );
        assertThat(interpretation.goalNeeds())
                .extracting(GoalNeed::trait)
                .contains(RecommendationTrait.BRIGHTENING_SUPPORT, RecommendationTrait.UV_PROTECTION);
        assertThat(interpretation.goalConflictNotices()).hasSize(1);
    }

    @Test
    void interpret_장벽이_높고_트러블_goal이면_goal_boost를_낮춘다() {
        SkinAnalysisResult result = SkinAnalysisResult.create(
                UserId.of("user-1"),
                List.of(
                        SkinMetricScore.of(SkinMetric.DRYNESS, 20),
                        SkinMetricScore.of(SkinMetric.BARRIER, 85),
                        SkinMetricScore.of(SkinMetric.OILINESS, 60),
                        SkinMetricScore.of(SkinMetric.BLEMISH_PRONENESS, 72),
                        SkinMetricScore.of(SkinMetric.SENSITIVITY, 30),
                        SkinMetricScore.of(SkinMetric.PIGMENTATION_TONE, 10),
                        SkinMetricScore.of(SkinMetric.AGING_SIGNS, 10)
                ),
                List.of("Q11_2")
        );

        SkinInterpretation interpretation = skinInterpreter.interpret(result);

        assertThat(interpretation.routineIntent()).isEqualTo(SkinInterpretation.RoutineIntent.BARRIER_RECOVERY);
        assertThat(interpretation.goalNeeds())
                .filteredOn(goalNeed -> goalNeed.trait() == RecommendationTrait.BLEMISH_CARE_SUPPORT)
                .extracting(GoalNeed::boost)
                .containsExactly(GoalNeed.Boost.LOW);
        assertThat(interpretation.riskConstraints())
                .filteredOn(risk -> risk.trait() == ProductRiskTrait.STRONG_EXFOLIATION_EFFECT)
                .extracting(RiskConstraint::policy)
                .contains(RiskConstraint.Policy.HARD_BLOCK);
        assertThat(interpretation.goalConflictNotices()).hasSize(1);
    }

    @Test
    void levelReader_모든_피부지표가_없으면_예외가_발생한다() {
        SkinMetricLevelReader levelReader = new SkinMetricLevelReader();

        assertThatThrownBy(() -> levelReader.read(List.of(
                SkinMetricScore.of(SkinMetric.DRYNESS, 50)
        ))).isInstanceOf(RecommendationException.class);
    }

    @Test
    void SkinInterpreter는_null_분석결과를_허용하지_않는다() {
        assertThatThrownBy(() -> skinInterpreter.interpret(null))
                .isInstanceOf(RecommendationException.class);
    }
}
