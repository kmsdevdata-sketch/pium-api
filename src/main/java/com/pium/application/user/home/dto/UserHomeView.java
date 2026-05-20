package com.pium.application.user.home.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserHomeView(
        String userName,
        long historyCount,
        LatestDiagnosisView latestDiagnosis
) {

    public record LatestDiagnosisView(
            String id,
            LocalDateTime createdAt,
            String summary,
            List<ResultMetricPreviewView> previewMetrics
    ) {
    }

    public record ResultMetricPreviewView(
            String key,
            String label,
            int score,
            String level
    ) {
    }
}
