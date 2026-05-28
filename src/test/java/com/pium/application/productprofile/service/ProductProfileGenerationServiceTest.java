package com.pium.application.productprofile.service;

import com.pium.application.product.required.LoadProductPort;
import com.pium.application.productprofile.required.GenerateProductProfilePort;
import com.pium.application.productprofile.required.SaveProductProfilePort;
import com.pium.domain.product.exception.ProductException;
import com.pium.domain.product.fixture.ProductFixture;
import com.pium.domain.product.model.Product;
import com.pium.domain.productprofile.fixture.ProductProfileFixture;
import com.pium.domain.productprofile.model.ProductProfile;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductProfileGenerationServiceTest {

    private final LoadProductPort loadProductPort = mock(LoadProductPort.class);
    private final GenerateProductProfilePort generateProductProfilePort = mock(GenerateProductProfilePort.class);
    private final SaveProductProfilePort saveProductProfilePort = mock(SaveProductProfilePort.class);
    private final ProductProfileGenerationService service = new ProductProfileGenerationService(
            loadProductPort,
            generateProductProfilePort,
            saveProductProfilePort
    );

    @Test
    void generate_Product를_조회하고_Profile을_생성_저장한다() {
        Product product = ProductFixture.createProduct();
        ProductProfile productProfile = ProductProfileFixture.createProductProfile(product.getId());

        when(loadProductPort.findById(product.getId())).thenReturn(Optional.of(product));
        when(generateProductProfilePort.generate(product)).thenReturn(productProfile);
        when(saveProductProfilePort.save(productProfile)).thenReturn(productProfile);

        var result = service.generate(product.getId());

        assertThat(result.productId()).isEqualTo(product.getId().value());
        assertThat(result.benefitTraits()).hasSize(1);
        assertThat(result.riskTraits()).hasSize(1);
        verify(loadProductPort).findById(product.getId());
        verify(generateProductProfilePort).generate(product);
        verify(saveProductProfilePort).save(productProfile);
    }

    @Test
    void generate_Product가_없으면_예외가_발생한다() {
        Product product = ProductFixture.createProduct();
        when(loadProductPort.findById(product.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generate(product.getId()))
                .isInstanceOf(ProductException.class);
    }
}
