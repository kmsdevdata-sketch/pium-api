package com.pium.application.product.required;

import com.pium.application.product.dto.ProductCommand;
import com.pium.domain.product.model.Product;
import com.pium.domain.product.vo.ProductId;

import java.util.List;
import java.util.Optional;

/**
 * Required Port
 * - 상품 원본 데이터를 조회하기 위한 포트
 */
public interface LoadProductPort {

    Optional<Product> findById(ProductId productId);

    List<Product> search(ProductCommand.Search command);
}
