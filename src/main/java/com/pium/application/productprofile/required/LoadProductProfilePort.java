package com.pium.application.productprofile.required;

import com.pium.domain.product.vo.ProductId;
import com.pium.domain.productprofile.model.ProductProfile;

import java.util.Optional;

/**
 * ProductProfile을 조회하기 위한 포트.
 */
public interface LoadProductProfilePort {

    Optional<ProductProfile> findByProductId(ProductId productId);
}
