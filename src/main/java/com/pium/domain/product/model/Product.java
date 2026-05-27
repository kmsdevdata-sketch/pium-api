package com.pium.domain.product.model;

import com.pium.domain.product.enumtype.FunctionalLabel;
import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.ProductStatus;
import com.pium.domain.product.enumtype.UsageStep;
import com.pium.domain.product.exception.ProductErrorCode;
import com.pium.domain.product.exception.ProductException;
import com.pium.domain.product.vo.ProductId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class Product {

    private final ProductId id;
    private final LocalDateTime createdAt;

    private String sourceUrl;
    private String brandName;
    private String productName;
    private ProductCategory category;
    private UsageStep usageStep;
    private int price;
    private String imageUrl;
    private String ingredientText;
    private String claims;
    private List<FunctionalLabel> functionalLabels;
    private ProductStatus status;
    private String adminMemo;
    private LocalDateTime updatedAt;

    private Product(
            ProductId id,
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
            LocalDateTime createdAt
    ) {
        validateSourceUrl(sourceUrl);
        validateBrandName(brandName);
        validateProductName(productName);
        validateCategory(category);
        validateUsageStep(usageStep);
        validatePrice(price);
        validateStatus(status);

        this.id = id;
        this.sourceUrl = sourceUrl.trim();
        this.brandName = brandName.trim();
        this.productName = productName.trim();
        this.category = category;
        this.usageStep = usageStep;
        this.price = price;
        this.imageUrl = normalizeNullable(imageUrl);
        this.ingredientText = normalizeNullable(ingredientText);
        this.claims = normalizeNullable(claims);
        this.functionalLabels = normalizeFunctionalLabels(functionalLabels);
        this.status = status;
        this.adminMemo = normalizeNullable(adminMemo);
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static Product create(
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
            String adminMemo
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new Product(
                ProductId.newId(),
                sourceUrl,
                brandName,
                productName,
                category,
                usageStep,
                price,
                imageUrl,
                ingredientText,
                claims,
                functionalLabels,
                ProductStatus.ACTIVE,
                adminMemo,
                now
        );
    }

    public static Product reconstitute(
            ProductId id,
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
        Product product = new Product(
                id,
                sourceUrl,
                brandName,
                productName,
                category,
                usageStep,
                price,
                imageUrl,
                ingredientText,
                claims,
                functionalLabels,
                status,
                adminMemo,
                createdAt
        );
        product.updatedAt = updatedAt;
        return product;
    }

    public void update(
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
            String adminMemo
    ) {
        validateSourceUrl(sourceUrl);
        validateBrandName(brandName);
        validateProductName(productName);
        validateCategory(category);
        validateUsageStep(usageStep);
        validatePrice(price);
        validateStatus(status);

        this.sourceUrl = sourceUrl.trim();
        this.brandName = brandName.trim();
        this.productName = productName.trim();
        this.category = category;
        this.usageStep = usageStep;
        this.price = price;
        this.imageUrl = normalizeNullable(imageUrl);
        this.ingredientText = normalizeNullable(ingredientText);
        this.claims = normalizeNullable(claims);
        this.functionalLabels = normalizeFunctionalLabels(functionalLabels);
        this.status = status;
        this.adminMemo = normalizeNullable(adminMemo);
        this.updatedAt = LocalDateTime.now();
    }

    private static void validateSourceUrl(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new ProductException(ProductErrorCode.INVALID_SOURCE_URL);
        }
    }

    private static void validateBrandName(String brandName) {
        if (brandName == null || brandName.isBlank()) {
            throw new ProductException(ProductErrorCode.INVALID_BRAND_NAME);
        }
    }

    private static void validateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_NAME);
        }
    }

    private static void validateCategory(ProductCategory category) {
        if (category == null) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_CATEGORY);
        }
    }

    private static void validateUsageStep(UsageStep usageStep) {
        if (usageStep == null) {
            throw new ProductException(ProductErrorCode.INVALID_USAGE_STEP);
        }
    }

    private static void validatePrice(int price) {
        if (price < 0) {
            throw new ProductException(ProductErrorCode.INVALID_PRICE);
        }
    }

    private static void validateStatus(ProductStatus status) {
        if (status == null) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_STATUS);
        }
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static List<FunctionalLabel> normalizeFunctionalLabels(List<FunctionalLabel> labels) {
        if (labels == null || labels.isEmpty()) {
            return List.of();
        }
        return labels.stream()
                .distinct()
                .toList();
    }
}
