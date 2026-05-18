package com.pium.adapter.outbound.skinanalysis.fixture;

import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;
import com.pium.domain.user.vo.UserId;

import java.util.List;

public final class SkinAnalysisResultFixture {

    private SkinAnalysisResultFixture() {
    }

    public static UserId createUserId() {
        return UserId.of("user-test-001");
    }

    public static List<SkinMetricScore> createSkinMetricScores() {
        return List.of(
                SkinMetricScore.of(SkinMetric.DRYNESS, 72),
                SkinMetricScore.of(SkinMetric.BARRIER, 68),
                SkinMetricScore.of(SkinMetric.OILINESS, 34),
                SkinMetricScore.of(SkinMetric.BLEMISH_PRONENESS, 41),
                SkinMetricScore.of(SkinMetric.SENSITIVITY, 63),
                SkinMetricScore.of(SkinMetric.PIGMENTATION_TONE, 29),
                SkinMetricScore.of(SkinMetric.AGING_SIGNS, 37)
        );
    }

    public static List<String> createGoals() {
        return List.of("Q11_1", "Q11_6");
    }

    public static SkinAnalysisResult createSkinAnalysisResult() {
        return SkinAnalysisResult.create(
                createUserId(),
                createSkinMetricScores(),
                createGoals()
        );
    }
}
