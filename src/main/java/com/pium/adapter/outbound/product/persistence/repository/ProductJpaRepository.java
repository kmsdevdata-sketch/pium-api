package com.pium.adapter.outbound.product.persistence.repository;

import com.pium.adapter.outbound.product.persistence.entity.ProductEntity;
import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, String>, JpaSpecificationExecutor<ProductEntity> {

    List<ProductEntity> findAllByOrderByCreatedAtDesc();

    List<ProductEntity> findAllByStatusOrderByCreatedAtDesc(ProductStatus status);

    List<ProductEntity> findAllByCategoryOrderByCreatedAtDesc(ProductCategory category);

    List<ProductEntity> findAllByStatusAndCategoryOrderByCreatedAtDesc(ProductStatus status, ProductCategory category);
}
