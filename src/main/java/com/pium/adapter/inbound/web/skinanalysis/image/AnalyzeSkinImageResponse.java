package com.pium.adapter.inbound.web.skinanalysis.image;

import com.pium.application.skinanalysis.image.dto.AnalyzeImageResultView;

import java.util.List;

/**
 * 사진 기반 피부 분석 응답 DTO.
 */
public record AnalyzeSkinImageResponse(
        String status,
        Integer retryAfterSeconds,
        List<SkinMetricScoreResponse> skinMetricScores
) {

    public static AnalyzeSkinImageResponse from(AnalyzeImageResultView view) {
        List<SkinMetricScoreResponse> mapped = view.skinMetricScores().stream()
                .map(score -> new SkinMetricScoreResponse(
                        score.metricKey(),
                        score.score()
                ))
                .toList();

        return new AnalyzeSkinImageResponse(
                view.status().name(),
                view.retryAfterSeconds(),
                mapped
        );
    }

    public record SkinMetricScoreResponse(
            String metricKey,
            int score
    ) {
    }
}
