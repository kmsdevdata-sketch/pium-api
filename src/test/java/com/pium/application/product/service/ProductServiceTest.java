package com.pium.application.product.service;

import com.pium.application.product.dto.ProductCommand;
import com.pium.application.product.required.LoadProductPort;
import com.pium.application.product.required.SaveProductPort;
import com.pium.domain.product.enumtype.FunctionalLabel;
import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.ProductStatus;
import com.pium.domain.product.enumtype.UsageStep;
import com.pium.domain.product.exception.ProductException;
import com.pium.domain.product.fixture.ProductFixture;
import com.pium.domain.product.model.Product;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private final SaveProductPort saveProductPort = mock(SaveProductPort.class);
    private final LoadProductPort loadProductPort = mock(LoadProductPort.class);
    private final ProductService productService = new ProductService(saveProductPort, loadProductPort);

    @Test
    void register_상품을_생성하고_저장한다() {
        when(saveProductPort.save(org.mockito.ArgumentMatchers.any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = productService.register(new ProductCommand.Register(
                "https://oliveyoung.example/products/1",
                "피움랩",
                "장벽 진정 크림",
                ProductCategory.LOTION_CREAM,
                UsageStep.MOISTURIZE,
                24000,
                null,
                "전성분",
                "claim",
                List.of(FunctionalLabel.BARRIER_FUNCTION_RECOVERY),
                "memo"
        ));

        assertThat(result.productId()).isNotBlank();
        assertThat(result.status()).isEqualTo(ProductStatus.ACTIVE);
        verify(saveProductPort).save(org.mockito.ArgumentMatchers.any(Product.class));
    }

    @Test
    void update_기존상품을_수정한다() {
        Product product = ProductFixture.createProduct();
        when(loadProductPort.findById(product.getId())).thenReturn(Optional.of(product));
        when(saveProductPort.save(product)).thenReturn(product);

        var result = productService.update(product.getId(), new ProductCommand.Update(
                "https://oliveyoung.example/products/2",
                "새브랜드",
                "수분 세럼",
                ProductCategory.ESSENCE_SERUM,
                UsageStep.TREAT,
                18000,
                null,
                "전성분2",
                "claim2",
                List.of(FunctionalLabel.BRIGHTENING),
                ProductStatus.INACTIVE,
                "memo2"
        ));

        assertThat(result.brandName()).isEqualTo("새브랜드");
        assertThat(result.status()).isEqualTo(ProductStatus.INACTIVE);
        verify(loadProductPort).findById(product.getId());
        verify(saveProductPort).save(product);
    }

    @Test
    void get_상품이_없으면_예외가_발생한다() {
        Product product = ProductFixture.createProduct();
        when(loadProductPort.findById(product.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.get(product.getId()))
                .isInstanceOf(ProductException.class);
    }

    @Test
    void list_상품목록을_반환한다() {
        Product product = ProductFixture.createProduct();
        ProductCommand.Search command = new ProductCommand.Search(ProductStatus.ACTIVE, null, "크림");
        when(loadProductPort.search(command)).thenReturn(List.of(product));

        var result = productService.list(command);

        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.products().get(0).productName()).isEqualTo("장벽 진정 크림");
    }
}
