package com.pium.application.product.dto;

import com.pium.domain.product.enumtype.FunctionalLabel;
import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.ProductStatus;
import com.pium.domain.product.enumtype.UsageStep;

import java.util.List;

public final class ProductCommand {

    private ProductCommand() {
    }

    public record Register(
            String sourceUrl,
            String brandName,
            String productName,
            ProductCategory category,
            UsageStep usageStep,
            String imageUrl,
            String ingredientText,
            String claims,
            List<FunctionalLabel> functionalLabels,
            String adminMemo
    ) {
    }

    public record Update(
            String sourceUrl,
            String brandName,
            String productName,
            ProductCategory category,
            UsageStep usageStep,
            String imageUrl,
            String ingredientText,
            String claims,
            List<FunctionalLabel> functionalLabels,
            ProductStatus status,
            String adminMemo
    ) {
    }

    public record Search(
            ProductStatus status,
            ProductCategory category,
            String keyword
    ) {
    }
}
