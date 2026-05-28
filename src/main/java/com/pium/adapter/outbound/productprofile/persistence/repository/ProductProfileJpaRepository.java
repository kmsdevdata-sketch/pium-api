package com.pium.adapter.outbound.productprofile.persistence.repository;

import com.pium.adapter.outbound.productprofile.persistence.entity.ProductProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductProfileJpaRepository extends JpaRepository<ProductProfileEntity,String> {
}
