package com.pium.adapter.inbound.web.user;

import com.pium.adapter.inbound.response.ApiDateTimeFormatter;
import com.pium.application.skinanalysis.result.dto.SkinAnalysisResultListView;

import java.util.List;

public record SkinAnalysisResultListResponse(
        long historyCount,
        List<ItemResponse> results
) {

    public static SkinAnalysisResultListResponse from(SkinAnalysisResultListView view) {
        return new SkinAnalysisResultListResponse(
                view.historyCount(),
                view.results().stream()
                        .map(item -> new ItemResponse(
                                item.resultId(),
                                ApiDateTimeFormatter.format(item.createdAt()),
                                item.oneLiner(),
                                new TopMetricResponse(
                                        item.topMetric().key(),
                                        item.topMetric().label(),
                                        item.topMetric().level()
                                )
                        ))
                        .toList()
        );
    }

    public record ItemResponse(
            String resultId,
            String createdAt,
            String oneLiner,
            TopMetricResponse topMetric
    ) {
    }

    public record TopMetricResponse(
            String key,
            String label,
            String level
    ) {
    }
}
