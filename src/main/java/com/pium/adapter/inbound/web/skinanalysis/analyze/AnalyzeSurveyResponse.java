package com.pium.adapter.inbound.web.skinanalysis.analyze;

import com.pium.application.skinanalysis.analyze.dto.AnalyzeResultView;

import java.util.List;

/**
 * 설문 분석 응답 DTO (Inbound Adapter)
 */
public record AnalyzeSurveyResponse(
        List<SkinMetricScoreResponse> skinMetricScores
) {
    public static AnalyzeSurveyResponse from(AnalyzeResultView view) {
        List<SkinMetricScoreResponse> mapped = view.skinMetricScores().stream()
                .map(v -> new SkinMetricScoreResponse(v.metricKey(), v.score()))
                .toList();
        return new AnalyzeSurveyResponse(mapped);
    }

    public record SkinMetricScoreResponse(
            String metricKey,
            int score
    ) {
    }
}