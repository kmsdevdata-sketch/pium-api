package com.pium.application.skinanalysis.result.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SkinAnalysisResultListView(
        long historyCount,
        List<ItemView> results
) {

    public record ItemView(
            String resultId,
            LocalDateTime createdAt,
            String oneLiner,
            TopMetricView topMetric
    ) {
    }

    public record TopMetricView(
            String key,
            String label,
            String level
    ) {
    }
}
