package com.pium.application.productprofile.service;

import com.pium.application.productprofile.dto.ProductProfileView;
import com.pium.application.productprofile.provided.GetProductProfile;
import com.pium.application.productprofile.required.LoadProductProfilePort;
import com.pium.domain.product.exception.ProductErrorCode;
import com.pium.domain.product.exception.ProductException;
import com.pium.domain.product.vo.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ProductProfile 조회 유즈케이스 구현체.
 */
@Service
@RequiredArgsConstructor
public class GetProductProfileService implements GetProductProfile {

    private final LoadProductProfilePort loadProductProfilePort;

    @Override
    public ProductProfileView get(ProductId productId) {
        return loadProductProfilePort.findByProductId(productId)
                .map(ProductProfileView::from)
                .orElseThrow(() -> new ProductException(ProductErrorCode.INVALID_PRODUCT_ID));
    }
}
