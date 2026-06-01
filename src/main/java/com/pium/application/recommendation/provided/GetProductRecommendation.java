package com.pium.application.recommendation.provided;

import com.pium.application.recommendation.dto.ProductRecommendationDetailView;
import com.pium.application.recommendation.dto.ProductRecommendationListView;
import com.pium.domain.user.vo.UserId;

public interface GetProductRecommendation {

    ProductRecommendationListView getLatest(UserId userId, String category);

    ProductRecommendationDetailView getDetail(UserId userId, String productId);
}
