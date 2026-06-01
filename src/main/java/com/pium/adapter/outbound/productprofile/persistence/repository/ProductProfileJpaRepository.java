package com.pium.adapter.outbound.productprofile.persistence.repository;

import com.pium.adapter.outbound.productprofile.persistence.entity.ProductProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ProductProfileJpaRepository extends JpaRepository<ProductProfileEntity,String> {
    @Query("""
            select profile
            from ProductProfileEntity profile
            join ProductEntity product on product.productId = profile.productId
            where product.status = com.pium.domain.product.enumtype.ProductStatus.ACTIVE
            """)
    List<ProductProfileEntity> findAllActiveProfiles();
}
