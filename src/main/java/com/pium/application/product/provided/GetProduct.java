package com.pium.application.product.provided;

import com.pium.application.product.dto.ProductView;
import com.pium.domain.product.vo.ProductId;

/**
 * 등록된 상품 상세 정보를 조회하는 유즈케이스 진입 포트.
 */
public interface GetProduct {

    ProductView get(ProductId productId);
}
