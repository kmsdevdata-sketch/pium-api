package com.pium.application.productprofile.service;

import com.pium.application.productprofile.required.LoadProductProfilePort;
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
import static org.mockito.Mockito.when;

class GetProductProfileServiceTest {

    private final LoadProductProfilePort loadProductProfilePort = mock(LoadProductProfilePort.class);
    private final GetProductProfileService service = new GetProductProfileService(loadProductProfilePort);

    @Test
    void get_ProductProfile을_조회한다() {
        Product product = ProductFixture.createProduct();
        ProductProfile productProfile = ProductProfileFixture.createProductProfile(product.getId());
        when(loadProductProfilePort.findByProductId(product.getId())).thenReturn(Optional.of(productProfile));

        var result = service.get(product.getId());

        assertThat(result.productId()).isEqualTo(product.getId().value());
        assertThat(result.benefitTraits()).hasSize(1);
        assertThat(result.evidenceSignals()).hasSize(2);
    }

    @Test
    void get_ProductProfile이_없으면_예외가_발생한다() {
        Product product = ProductFixture.createProduct();
        when(loadProductProfilePort.findByProductId(product.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(product.getId()))
                .isInstanceOf(ProductException.class);
    }
}
