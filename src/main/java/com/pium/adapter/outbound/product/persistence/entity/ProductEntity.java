package com.pium.adapter.outbound.product.persistence.entity;

import com.pium.domain.product.enumtype.FunctionalLabel;
import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.ProductStatus;
import com.pium.domain.product.enumtype.UsageStep;
import com.pium.domain.product.model.Product;
import com.pium.domain.product.vo.ProductId;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductEntity {

    @Id
    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Column(name = "source_url", nullable = false, length = 1024)
    private String sourceUrl;

    @Column(name = "brand_name", nullable = false, length = 100)
    private String brandName;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 64)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_step", nullable = false, length = 64)
    private UsageStep usageStep;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Column(name = "ingredient_text", columnDefinition = "TEXT")
    private String ingredientText;

    @Column(name = "claims", columnDefinition = "TEXT")
    private String claims;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "product_functional_label",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "functional_label", nullable = false, length = 64)
    private List<FunctionalLabel> functionalLabels;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ProductStatus status;

    @Column(name = "admin_memo", columnDefinition = "TEXT")
    private String adminMemo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private ProductEntity(
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
        this.productId = productId;
        this.sourceUrl = sourceUrl;
        this.brandName = brandName;
        this.productName = productName;
        this.category = category;
        this.usageStep = usageStep;
        this.price = price;
        this.imageUrl = imageUrl;
        this.ingredientText = ingredientText;
        this.claims = claims;
        this.functionalLabels = List.copyOf(functionalLabels);
        this.status = status;
        this.adminMemo = adminMemo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductEntity from(Product product) {
        return new ProductEntity(
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

    public Product toDomain() {
        return Product.reconstitute(
                ProductId.of(productId),
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
                createdAt,
                updatedAt
        );
    }
}
