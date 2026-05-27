package com.pium.application.product.dto;

import java.util.List;

public record ProductListView(
        long totalCount,
        List<ProductView> products
) {
}
