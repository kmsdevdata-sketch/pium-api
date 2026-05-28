package com.pium.adapter.inbound.web.admin.product;

import com.pium.application.productprofile.dto.ProductProfileGenerationView;

public record ProductProfileGenerationResponse(
        String productId,
        boolean generated
) {

    public static ProductProfileGenerationResponse from(ProductProfileGenerationView view) {
        return new ProductProfileGenerationResponse(view.productId(), view.generated());
    }
}
