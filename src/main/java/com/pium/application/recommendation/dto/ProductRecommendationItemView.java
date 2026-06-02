package com.pium.application.recommendation.dto;

import java.util.List;

public record ProductRecommendationItemView(
        int rank,
        String productId,
        String brandName,
        String productName,
        String imageUrl,
        String category,
        String categoryLabel,
        String usageStep,
        String usageStepLabel,
        String scoreBand,
        String scoreBandLabel,
        String recommendationReason,
        List<String> careTags,
        List<String> cautionPoints
) {
}
