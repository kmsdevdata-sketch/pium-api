package com.pium.adapter.inbound.web.admin.product;

import com.pium.adapter.inbound.response.ApiDateTimeFormatter;
import com.pium.application.product.dto.ProductView;

import java.util.List;

public record ProductResponse(
        String productId,
        String sourceUrl,
        String brandName,
        String productName,
        String category,
        String usageStep,
        String imageUrl,
        String ingredientText,
        String claims,
        List<String> functionalLabels,
        String status,
        String adminMemo,
        String createdAt,
        String updatedAt
) {

    public static ProductResponse from(ProductView view) {
        return new ProductResponse(
                view.productId(),
                view.sourceUrl(),
                view.brandName(),
                view.productName(),
                view.category().name(),
                view.usageStep().name(),
                view.imageUrl(),
                view.ingredientText(),
                view.claims(),
                view.functionalLabels().stream()
                        .map(Enum::name)
                        .toList(),
                view.status().name(),
                view.adminMemo(),
                ApiDateTimeFormatter.format(view.createdAt()),
                ApiDateTimeFormatter.format(view.updatedAt())
        );
    }
}
