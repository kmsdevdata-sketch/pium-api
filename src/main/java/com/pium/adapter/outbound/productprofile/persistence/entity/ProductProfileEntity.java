package com.pium.adapter.outbound.productprofile.persistence.entity;

import com.pium.domain.product.vo.ProductId;
import com.pium.domain.productprofile.model.ProductProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ProductProfile JPA 엔티티.
 * 초기 MVP단계이고 중간 해석과정에서 파생되는 엔티티이다 보니
 * 변경 가능성이 높다고 판단 & AI 출력 schema조정가능성 고려 JSON형태 저장
 */
@Entity
@Table(name = "product_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductProfileEntity {

    @Id
    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Lob
    @Column(name = "profile_json", nullable = false, columnDefinition = "LONGTEXT")
    private String profileJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private ProductProfileEntity(
            String productId,
            String profileJson,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.productId = productId;
        this.profileJson = profileJson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductProfileEntity from(ProductProfile productProfile, ObjectMapper objectMapper) {
        LocalDateTime now = LocalDateTime.now();

        try {
            return new ProductProfileEntity(
                    productProfile.productId().value(),
                    objectMapper.writeValueAsString(productProfile),
                    now,
                    now
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("ProductProfile JSON 직렬화에 실패했습니다.", e);
        }
    }

    public ProductProfile toDomain(ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(profileJson, ProductProfile.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("ProductProfile JSON 역직렬화에 실패했습니다.", e);
        }
    }

    public void update(ProductProfile productProfile, ObjectMapper objectMapper) {
        if (!productId.equals(productProfile.productId().value())) {
            throw new IllegalArgumentException("ProductProfile productId가 일치하지 않습니다.");
        }

        try {
            this.profileJson = objectMapper.writeValueAsString(productProfile);
            this.updatedAt = LocalDateTime.now();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("ProductProfile JSON 직렬화에 실패했습니다.", e);
        }
    }

    public ProductId productId() {
        return ProductId.of(productId);
    }
}