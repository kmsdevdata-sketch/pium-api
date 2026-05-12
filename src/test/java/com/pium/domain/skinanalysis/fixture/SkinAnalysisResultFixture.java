package com.pium.domain.skinanalysis.fixture;

import com.pium.domain.skinanalysis.enumtype.IngredientGroup;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import com.pium.domain.skinanalysis.vo.RequiredIngredient;
import com.pium.domain.skinanalysis.vo.RulesVersion;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;
import com.pium.domain.user.vo.UserId;

import java.util.List;

public class SkinAnalysisResultFixture {

    public static UserId createUserId() {
        return UserId.newId();
    }

    public static RulesVersion createRulesVersion() {
        return RulesVersion.of("v0.1.0");
    }

    public static List<SkinMetricScore> createSkinMetricScores() {
        return List.of(
                SkinMetricScore.of(SkinMetric.DRYNESS, 70),
                SkinMetricScore.of(SkinMetric.OILINESS, 35),
                SkinMetricScore.of(SkinMetric.SENSITIVITY, 60)
        );
    }

    public static List<RequiredIngredient> createRequiredIngredients() {
        return List.of(
                RequiredIngredient.of(IngredientGroup.HYDRATION, 80),
                RequiredIngredient.of(IngredientGroup.BARRIER_SUPPORT, 75),
                RequiredIngredient.of(IngredientGroup.CALMING_SENSITIVITY_SUPPORT, 65)
        );
    }
}
