package com.pium.application.recommendation.required;

import com.pium.domain.productprofile.model.ProductProfile;
import com.pium.domain.recommendation.model.ProductSearchSpec;

import java.util.List;

/**
 * 추천 계산에 사용할 ProductProfile 후보를 조회하는 포트
 */
public interface LoadRecommendationProductProfilePort {

    List<ProductProfile> loadCandidates(ProductSearchSpec spec);
}
