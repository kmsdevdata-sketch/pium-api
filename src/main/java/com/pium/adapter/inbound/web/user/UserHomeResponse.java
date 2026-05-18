package com.pium.adapter.inbound.web.user;

import com.pium.adapter.inbound.response.ApiDateTimeFormatter;
import com.pium.application.user.home.dto.UserHomeView;

import java.util.List;

public record UserHomeResponse(
        long historyCount,
        LatestDiagnosisResponse latestDiagnosis
) {

    public static UserHomeResponse from(UserHomeView view) {
        return new UserHomeResponse(
                view.historyCount(),
                view.latestDiagnosis() == null ? null : new LatestDiagnosisResponse(
                        view.latestDiagnosis().id(),
                        ApiDateTimeFormatter.format(view.latestDiagnosis().createdAt()),
                        view.latestDiagnosis().summary(),
                        view.latestDiagnosis().previewMetrics().stream()
                                .map(metric -> new ResultMetricPreviewResponse(
                                        metric.key(),
                                        metric.label(),
                                        metric.score(),
                                        metric.level()
                                ))
                                .toList()
                )
        );
    }

    public record LatestDiagnosisResponse(
            String id,
            String createdAt,
            String summary,
            List<ResultMetricPreviewResponse> previewMetrics
    ) {
    }

    public record ResultMetricPreviewResponse(
            String key,
            String label,
            int score,
            String level
    ) {
    }
}
