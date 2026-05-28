package com.pium.application.productprofile.dto;

import com.pium.domain.productprofile.model.ProductProfile;

/**
 * ProductProfile 생성 결과 응답 모델.
 */
public record ProductProfileGenerationView(
        String productId,
        boolean generated
) {

    public static ProductProfileGenerationView from(ProductProfile productProfile) {
        return new ProductProfileGenerationView(productProfile.productId().value(), true);
    }
}
