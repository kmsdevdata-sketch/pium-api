package com.pium.application.recommendation.dto;

import java.util.List;

public record ProductRecommendationDetailView(
        String productId,
        String brandName,
        String productName,
        String imageUrl,
        String sourceUrl,
        String category,
        String categoryLabel,
        String usageStep,
        String usageStepLabel,
        String scoreBand,
        String scoreBandLabel,
        List<ReasonDetailView> reasonDetails,
        List<String> recommendationReasons,
        List<String> cautions,
        List<TagView> careTags,
        List<TagView> cautionPoints,
        String adDisclosure
) {

    public record ReasonDetailView(
            String title,
            String body
    ) {
    }

    public record TagView(
            String key,
            String label
    ) {
    }
}
