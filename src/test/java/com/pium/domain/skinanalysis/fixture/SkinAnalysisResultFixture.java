package com.pium.domain.skinanalysis.fixture;

import com.pium.domain.skinanalysis.enumtype.IngredientGroup;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;
import com.pium.domain.user.vo.UserId;

import java.util.List;

public class SkinAnalysisResultFixture {

    public static UserId createUserId() {
        return UserId.newId();
    }


    public static List<SkinMetricScore> createSkinMetricScores() {
        return List.of(
                SkinMetricScore.of(SkinMetric.DRYNESS, 70),
                SkinMetricScore.of(SkinMetric.OILINESS, 35),
                SkinMetricScore.of(SkinMetric.SENSITIVITY, 60)
        );
    }

    public static List<String> createGoals() {
        return List.of(
                "Q_GOAL_1",
                "Q_GOAL_2"
        );
    }
}
