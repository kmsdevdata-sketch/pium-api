package com.pium.application.productprofile.provided;

import com.pium.application.productprofile.dto.ProductProfileGenerationView;
import com.pium.domain.product.vo.ProductId;

/**
 * ProductProfile 생성 유즈케이스 진입 포트.
 */
public interface GenerateProductProfile {

    ProductProfileGenerationView generate(ProductId productId);
}
