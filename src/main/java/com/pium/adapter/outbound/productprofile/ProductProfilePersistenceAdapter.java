package com.pium.adapter.outbound.productprofile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pium.adapter.outbound.productprofile.persistence.entity.ProductProfileEntity;
import com.pium.adapter.outbound.productprofile.persistence.repository.ProductProfileJpaRepository;
import com.pium.application.productprofile.required.LoadProductProfilePort;
import com.pium.application.productprofile.required.SaveProductProfilePort;
import com.pium.domain.product.vo.ProductId;
import com.pium.domain.productprofile.model.ProductProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class ProductProfilePersistenceAdapter implements SaveProductProfilePort, LoadProductProfilePort {

    private final ProductProfileJpaRepository productProfileJpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ProductProfile save(ProductProfile productProfile) {
        ProductProfileEntity entity = productProfileJpaRepository.findById(productProfile.productId().value())
                .map(existing -> {
                    existing.update(productProfile, objectMapper);
                    return existing;
                })
                .orElseGet(() -> ProductProfileEntity.from(productProfile, objectMapper));

        return productProfileJpaRepository.save(entity).toDomain(objectMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductProfile> findByProductId(ProductId productId) {
        return productProfileJpaRepository.findById(productId.value())
                .map(entity -> entity.toDomain(objectMapper));
    }

}
