package com.pium.application.productprofile.service;

import com.pium.application.product.required.LoadProductPort;
import com.pium.application.productprofile.dto.ProductProfileView;
import com.pium.application.productprofile.provided.GenerateProductProfile;
import com.pium.application.productprofile.required.GenerateProductProfilePort;
import com.pium.application.productprofile.required.SaveProductProfilePort;
import com.pium.domain.product.exception.ProductErrorCode;
import com.pium.domain.product.exception.ProductException;
import com.pium.domain.product.model.Product;
import com.pium.domain.product.vo.ProductId;
import com.pium.domain.productprofile.model.ProductProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductProfileGenerationService implements GenerateProductProfile {

    private final LoadProductPort loadProductPort;
    private final GenerateProductProfilePort generateProductProfilePort;
    private final SaveProductProfilePort saveProductProfilePort;

    @Override
    public ProductProfileView generate(ProductId productId) {
        Product product = loadProductPort.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.INVALID_PRODUCT_ID));

        ProductProfile productProfile = generateProductProfilePort.generate(product);
        ProductProfile savedProfile = saveProductProfilePort.save(generateProductProfilePort);

        return ProductProfileView.from(savedProfile);
    }
}
