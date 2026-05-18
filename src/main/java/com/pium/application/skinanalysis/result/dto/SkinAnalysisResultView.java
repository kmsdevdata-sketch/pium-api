package com.pium.application.skinanalysis.result.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SkinAnalysisResultView(
        String resultId,
        LocalDateTime createdAt,
        String oneLiner,
        List<SkinMetricScoreView> skinMetricScores,
        List<CategoryDetailView> categoryDetails,
        String summary
) {

    public record SkinMetricScoreView(
            String metricKey,
            int score,
            String level
    ) {
    }

    public record CategoryDetailView(
            String metricKey,
            int score,
            String level,
            String stateText,
            String insight
    ) {
    }
}
