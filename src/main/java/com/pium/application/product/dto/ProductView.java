package com.pium.application.product.dto;

import com.pium.domain.product.enumtype.FunctionalLabel;
import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.ProductStatus;
import com.pium.domain.product.enumtype.UsageStep;
import com.pium.domain.product.model.Product;

import java.time.LocalDateTime;
import java.util.List;

public record ProductView(
        String productId,
        String sourceUrl,
        String brandName,
        String productName,
        ProductCategory category,
        UsageStep usageStep,
        int price,
        String imageUrl,
        String ingredientText,
        String claims,
        List<FunctionalLabel> functionalLabels,
        ProductStatus status,
        String adminMemo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductView from(Product product) {
        return new ProductView(
                product.getId().value(),
                product.getSourceUrl(),
                product.getBrandName(),
                product.getProductName(),
                product.getCategory(),
                product.getUsageStep(),
                product.getPrice(),
                product.getImageUrl(),
                product.getIngredientText(),
                product.getClaims(),
                product.getFunctionalLabels(),
                product.getStatus(),
                product.getAdminMemo(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
