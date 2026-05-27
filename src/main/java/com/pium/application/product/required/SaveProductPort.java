package com.pium.application.product.required;

import com.pium.domain.product.model.Product;

/**
 * Required Port
 * - 상품 원본 데이터를 영속화하기 위한 저장 포트
 */
public interface SaveProductPort {

    Product save(Product product);
}
