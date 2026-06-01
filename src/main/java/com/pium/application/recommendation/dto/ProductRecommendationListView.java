package com.pium.application.recommendation.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductRecommendationListView(
        String analysisResultId,
        BasedOnView basedOn,
        RecommendationSummaryView recommendationSummary,
        String adDisclosure,
        FilterView filters,
        List<ProductRecommendationItemView> topRecommendations,
        List<ProductRecommendationItemView> recommendations
) {

    public record BasedOnView(
            LocalDateTime createdAt,
            String summary
    ) {
    }

    public record RecommendationSummaryView(
            String headline,
            List<String> reasons,
            List<String> notices
    ) {
    }

    public record FilterView(
            String selectedCategory,
            List<CategoryFilterView> categories
    ) {
    }

    public record CategoryFilterView(
            String key,
            String label
    ) {
    }
}
