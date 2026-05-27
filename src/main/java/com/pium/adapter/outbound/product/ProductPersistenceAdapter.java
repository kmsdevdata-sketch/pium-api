package com.pium.adapter.outbound.product;

import com.pium.adapter.outbound.product.persistence.entity.ProductEntity;
import com.pium.adapter.outbound.product.persistence.repository.ProductJpaRepository;
import com.pium.application.product.dto.ProductCommand;
import com.pium.application.product.required.LoadProductPort;
import com.pium.application.product.required.SaveProductPort;
import com.pium.domain.product.model.Product;
import com.pium.domain.product.vo.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Outbound Adapter
 * - Product aggregate를 영속 모델로 변환해 저장하고 조회한다.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements SaveProductPort, LoadProductPort {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(ProductEntity.from(product)).toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(ProductId productId) {
        return productJpaRepository.findById(productId.value())
                .map(ProductEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> search(ProductCommand.Search command) {
        List<ProductEntity> products = findByCondition(command);
        return products.stream()
                .filter(product -> matchesKeyword(product, command.keyword()))
                .map(ProductEntity::toDomain)
                .toList();
    }

    private List<ProductEntity> findByCondition(ProductCommand.Search command) {
        if (command.status() != null && command.category() != null) {
            return productJpaRepository.findAllByStatusAndCategoryOrderByCreatedAtDesc(command.status(), command.category());
        }
        if (command.status() != null) {
            return productJpaRepository.findAllByStatusOrderByCreatedAtDesc(command.status());
        }
        if (command.category() != null) {
            return productJpaRepository.findAllByCategoryOrderByCreatedAtDesc(command.category());
        }
        return productJpaRepository.findAllByOrderByCreatedAtDesc();
    }

    private boolean matchesKeyword(ProductEntity product, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        return product.getBrandName().toLowerCase(Locale.ROOT).contains(normalized)
                || product.getProductName().toLowerCase(Locale.ROOT).contains(normalized);
    }
}
