package com.pium.application.product.provided;

import com.pium.application.product.dto.ProductCommand;
import com.pium.application.product.dto.ProductView;

/**
 * 상품 원본 데이터를 등록하는 유즈케이스 진입 포트.
 */
public interface RegisterProduct {

    ProductView register(ProductCommand.Register command);
}
