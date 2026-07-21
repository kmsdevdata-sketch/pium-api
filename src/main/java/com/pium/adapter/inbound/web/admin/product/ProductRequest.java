package com.pium.adapter.inbound.web.admin.product;

import com.pium.application.product.dto.ProductCommand;
import com.pium.domain.product.enumtype.FunctionalLabel;
import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.ProductStatus;
import com.pium.domain.product.enumtype.UsageStep;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class ProductRequest {

    private ProductRequest() {
    }

    public record Register(
            @NotBlank
            String sourceUrl,

            @NotBlank
            String brandName,

            @NotBlank
            String productName,

            @NotBlank
            String category,

            @NotBlank
            String usageStep,

            String imageUrl,
            String ingredientText,
            String claims,
            List<String> functionalLabels,
            String adminMemo
    ) {
        public ProductCommand.Register toCommand() {
            return new ProductCommand.Register(
                    sourceUrl,
                    brandName,
                    productName,
                    ProductCategory.of(category),
                    UsageStep.of(usageStep),
                    imageUrl,
                    ingredientText,
                    claims,
                    toFunctionalLabels(functionalLabels),
                    adminMemo
            );
        }
    }

    public record Update(
            @NotBlank
            String sourceUrl,

            @NotBlank
            String brandName,

            @NotBlank
            String productName,

            @NotBlank
            String category,

            @NotBlank
            String usageStep,

            String imageUrl,
            String ingredientText,
            String claims,
            List<String> functionalLabels,

            @NotBlank
            String status,

            String adminMemo
    ) {
        public ProductCommand.Update toCommand() {
            return new ProductCommand.Update(
                    sourceUrl,
                    brandName,
                    productName,
                    ProductCategory.of(category),
                    UsageStep.of(usageStep),
                    imageUrl,
                    ingredientText,
                    claims,
                    toFunctionalLabels(functionalLabels),
                    ProductStatus.of(status),
                    adminMemo
            );
        }
    }

    private static List<FunctionalLabel> toFunctionalLabels(List<String> labels) {
        if (labels == null || labels.isEmpty()) {
            return List.of();
        }
        return labels.stream()
                .map(FunctionalLabel::of)
                .distinct()
                .toList();
    }
}
