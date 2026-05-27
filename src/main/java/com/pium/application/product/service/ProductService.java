package com.pium.application.product.service;

import com.pium.application.product.dto.ProductCommand;
import com.pium.application.product.dto.ProductListView;
import com.pium.application.product.dto.ProductView;
import com.pium.application.product.provided.GetProduct;
import com.pium.application.product.provided.ListProducts;
import com.pium.application.product.provided.RegisterProduct;
import com.pium.application.product.provided.UpdateProduct;
import com.pium.application.product.required.LoadProductPort;
import com.pium.application.product.required.SaveProductPort;
import com.pium.domain.product.exception.ProductErrorCode;
import com.pium.domain.product.exception.ProductException;
import com.pium.domain.product.model.Product;
import com.pium.domain.product.vo.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 상품 어드민 유즈케이스 구현체.
 * - ProductProfile 생성 전 단계의 상품 원본 데이터를 관리한다.
 */
@Service
@RequiredArgsConstructor
public class ProductService implements RegisterProduct, UpdateProduct, GetProduct, ListProducts {

    private final SaveProductPort saveProductPort;
    private final LoadProductPort loadProductPort;

    @Override
    public ProductView register(ProductCommand.Register command) {
        Product product = Product.create(
                command.sourceUrl(),
                command.brandName(),
                command.productName(),
                command.category(),
                command.usageStep(),
                command.price(),
                command.imageUrl(),
                command.ingredientText(),
                command.claims(),
                command.functionalLabels(),
                command.adminMemo()
        );

        return ProductView.from(saveProductPort.save(product));
    }

    @Override
    public ProductView update(ProductId productId, ProductCommand.Update command) {
        Product product = loadProductPort.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.INVALID_PRODUCT_ID));

        product.update(
                command.sourceUrl(),
                command.brandName(),
                command.productName(),
                command.category(),
                command.usageStep(),
                command.price(),
                command.imageUrl(),
                command.ingredientText(),
                command.claims(),
                command.functionalLabels(),
                command.status(),
                command.adminMemo()
        );

        return ProductView.from(saveProductPort.save(product));
    }

    @Override
    public ProductView get(ProductId productId) {
        return loadProductPort.findById(productId)
                .map(ProductView::from)
                .orElseThrow(() -> new ProductException(ProductErrorCode.INVALID_PRODUCT_ID));
    }

    @Override
    public ProductListView list(ProductCommand.Search command) {
        var products = loadProductPort.search(command).stream()
                .map(ProductView::from)
                .toList();
        return new ProductListView(products.size(), products);
    }
}
