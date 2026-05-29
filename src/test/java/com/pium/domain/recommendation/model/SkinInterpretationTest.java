package com.pium.domain.recommendation.model;

import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.recommendation.exception.RecommendationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkinInterpretationTest {

    @Test
    void SkinInterpretation은_리스트를_불변으로_보관한다() {
        List<SkinNeed> primaryNeeds = new ArrayList<>();
        primaryNeeds.add(new SkinNeed(
                RecommendationTrait.HYDRATION_SUPPORT,
                SkinNeed.Intensity.REQUIRED,
                "DRYNESS_HIGH"
        ));

        SkinInterpretation interpretation = new SkinInterpretation(
                "result-1",
                SkinInterpretation.RoutineIntent.HYDRATION_BALANCE,
                primaryNeeds,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
        primaryNeeds.add(new SkinNeed(
                RecommendationTrait.BARRIER_SUPPORT,
                SkinNeed.Intensity.PREFERRED,
                "BARRIER_MID"
        ));

        assertThat(interpretation.primaryNeeds()).hasSize(1);
        assertThatThrownBy(() -> interpretation.primaryNeeds().add(new SkinNeed(
                RecommendationTrait.SOOTHING_SUPPORT,
                SkinNeed.Intensity.PREFERRED,
                "SENSITIVITY_MID"
        ))).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void SkinNeed는_필수값을_검증한다() {
        assertThatThrownBy(() -> new SkinNeed(null, SkinNeed.Intensity.REQUIRED, "DRYNESS_HIGH"))
                .isInstanceOf(RecommendationException.class);

        assertThatThrownBy(() -> new SkinNeed(
                RecommendationTrait.HYDRATION_SUPPORT,
                SkinNeed.Intensity.REQUIRED,
                " "
        )).isInstanceOf(RecommendationException.class);
    }

    @Test
    void RiskConstraint는_필수값을_검증한다() {
        assertThatThrownBy(() -> new RiskConstraint(
                ProductRiskTrait.FRAGRANCE_OR_ALLERGEN_RISK,
                null,
                "SENSITIVITY_HIGH"
        )).isInstanceOf(RecommendationException.class);
    }

    @Test
    void GoalNeed는_goal을_trim해서_보관한다() {
        GoalNeed goalNeed = new GoalNeed(
                " Q11_1 ",
                RecommendationTrait.HYDRATION_SUPPORT,
                GoalNeed.Boost.MEDIUM
        );

        assertThat(goalNeed.goal()).isEqualTo("Q11_1");
    }

    @Test
    void GoalConflictNotice는_필수값을_검증한다() {
        assertThatThrownBy(() -> new GoalConflictNotice(
                "Q11_2",
                "BARRIER_HIGH",
                ""
        )).isInstanceOf(RecommendationException.class);
    }
}
