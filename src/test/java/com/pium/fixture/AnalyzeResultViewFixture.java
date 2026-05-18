package com.pium.fixture;

import com.pium.application.skinanalysis.analyze.dto.AnalyzeResultView;

import java.util.List;

public class AnalyzeResultViewFixture {

    private AnalyzeResultViewFixture() {
    }

    public static List<AnalyzeResultView.SkinMetricScoreView> createSkinMetricScoreViews() {
        return List.of(
                new AnalyzeResultView.SkinMetricScoreView("DRYNESS", 72),
                new AnalyzeResultView.SkinMetricScoreView("BARRIER", 60),
                new AnalyzeResultView.SkinMetricScoreView("OILINESS", 35),
                new AnalyzeResultView.SkinMetricScoreView("BLEMISH_PRONENESS", 40),
                new AnalyzeResultView.SkinMetricScoreView("SENSITIVITY", 55),
                new AnalyzeResultView.SkinMetricScoreView("PIGMENTATION_TONE", 28),
                new AnalyzeResultView.SkinMetricScoreView("AGING_SIGNS", 33)
        );
    }

    public static AnalyzeResultView createAnalyzeResultView() {
        return new AnalyzeResultView(createSkinMetricScoreViews());
    }
}
