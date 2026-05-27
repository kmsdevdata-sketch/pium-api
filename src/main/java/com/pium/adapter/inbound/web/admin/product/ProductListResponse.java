package com.pium.adapter.inbound.web.admin.product;

import com.pium.application.product.dto.ProductListView;

import java.util.List;

public record ProductListResponse(
        long totalCount,
        List<ProductResponse> products
) {

    public static ProductListResponse from(ProductListView view) {
        return new ProductListResponse(
                view.totalCount(),
                view.products().stream()
                        .map(ProductResponse::from)
                        .toList()
        );
    }
}
