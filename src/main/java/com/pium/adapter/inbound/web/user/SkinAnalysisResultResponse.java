package com.pium.adapter.inbound.web.user;

import com.pium.application.skinanalysis.result.dto.SkinAnalysisResultView;

import java.time.LocalDateTime;
import java.util.List;

public record SkinAnalysisResultResponse(
        String resultId,
        LocalDateTime createdAt,
        String oneLiner,
        List<SkinMetricScoreResponse> skinMetricScores,
        List<CategoryDetailResponse> categoryDetails,
        String summary
) {

    public static SkinAnalysisResultResponse from(SkinAnalysisResultView view) {
        return new SkinAnalysisResultResponse(
                view.resultId(),
                view.createdAt(),
                view.oneLiner(),
                view.skinMetricScores().stream()
                        .map(score -> new SkinMetricScoreResponse(
                                score.metricKey(),
                                score.score(),
                                score.level()
                        ))
                        .toList(),
                view.categoryDetails().stream()
                        .map(detail -> new CategoryDetailResponse(
                                detail.metricKey(),
                                detail.score(),
                                detail.level(),
                                detail.stateText(),
                                detail.insight()
                        ))
                        .toList(),
                view.summary()
        );
    }

    public record SkinMetricScoreResponse(
            String metricKey,
            int score,
            String level
    ) {
    }

    public record CategoryDetailResponse(
            String metricKey,
            int score,
            String level,
            String stateText,
            String insight
    ) {
    }
}
