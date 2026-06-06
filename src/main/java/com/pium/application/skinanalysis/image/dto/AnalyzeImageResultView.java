package com.pium.application.skinanalysis.image.dto;

import com.pium.application.skinanalysis.analyze.dto.AnalyzeResultView;

import java.util.List;

public record AnalyzeImageResultView(
        Status status,
        Integer retryAfterSeconds,
        List<AnalyzeResultView.SkinMetricScoreView> skinMetricScores
) {

    private static final int DEFAULT_RETRY_AFTER_SECONDS = 2;

    public static AnalyzeImageResultView completed(AnalyzeResultView view) {
        return new AnalyzeImageResultView(Status.COMPLETED, null, view.skinMetricScores());
    }

    public static AnalyzeImageResultView processing() {
        return new AnalyzeImageResultView(Status.PROCESSING, DEFAULT_RETRY_AFTER_SECONDS, List.of());
    }

    public enum Status {
        PROCESSING,
        COMPLETED
    }
}
