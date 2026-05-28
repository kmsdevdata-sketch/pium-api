package com.pium.application.productprofile.required;

import com.pium.domain.product.model.Product;
import com.pium.domain.productprofile.model.ProductProfile;

/**
 * 상품 원본 데이터로 ProductProfile 초안을 생성하는 포트.
 */
public interface GenerateProductProfilePort {

    ProductProfile generate(Product product);
}
