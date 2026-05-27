package com.pium.application.product.provided;

import com.pium.application.product.dto.ProductCommand;
import com.pium.application.product.dto.ProductListView;

/**
 * 어드민 상품 목록을 조회하는 유즈케이스 진입 포트.
 */
public interface ListProducts {

    ProductListView list(ProductCommand.Search command);
}
