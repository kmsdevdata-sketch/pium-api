package com.pium.adapter.outbound.recommendation.productprofile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pium.adapter.outbound.product.ProductPersistenceAdapter;
import com.pium.adapter.outbound.productprofile.ProductProfilePersistenceAdapter;
import com.pium.domain.product.enumtype.ProductStatus;
import com.pium.domain.product.fixture.ProductFixture;
import com.pium.domain.product.model.Product;
import com.pium.domain.productprofile.fixture.ProductProfileFixture;
import com.pium.domain.productprofile.model.ProductProfile;
import com.pium.domain.recommendation.model.search.ProductSearchSpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        ProductPersistenceAdapter.class,
        ProductProfilePersistenceAdapter.class,
        RecommendationProductProfileQueryAdapter.class,
        RecommendationProductProfileQueryAdapterTest.ObjectMapperTestConfig.class
})
class RecommendationProductProfileQueryAdapterTest {

    @Autowired
    private ProductPersistenceAdapter productPersistenceAdapter;

    @Autowired
    private ProductProfilePersistenceAdapter productProfilePersistenceAdapter;

    @Autowired
    private RecommendationProductProfileQueryAdapter adapter;

    @Test
    void loadCandidates_ACTIVE_상품의_ProductProfile_후보를_조회한다() {
        Product product = productPersistenceAdapter.save(ProductFixture.createProduct());
        ProductProfile productProfile = ProductProfileFixture.createProductProfile(product.getId());
        productProfilePersistenceAdapter.save(productProfile);

        List<ProductProfile> candidates = adapter.loadCandidates(emptySpec());

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).productId()).isEqualTo(product.getId());
    }

    @Test
    void loadCandidates_INACTIVE_상품의_ProductProfile은_제외한다() {
        Product product = ProductFixture.createProduct();
        product.update(
                product.getSourceUrl(),
                product.getBrandName(),
                product.getProductName(),
                product.getCategory(),
                product.getUsageStep(),
                product.getPrice(),
                product.getImageUrl(),
                product.getIngredientText(),
                product.getClaims(),
                product.getFunctionalLabels(),
                ProductStatus.INACTIVE,
                product.getAdminMemo()
        );
        Product savedProduct = productPersistenceAdapter.save(product);
        ProductProfile productProfile = ProductProfileFixture.createProductProfile(savedProduct.getId());
        productProfilePersistenceAdapter.save(productProfile);

        List<ProductProfile> candidates = adapter.loadCandidates(emptySpec());

        assertThat(candidates).isEmpty();
    }

    @Test
    void loadCandidates_ProductProfile이_없는_ACTIVE_상품은_조회되지_않는다() {
        productPersistenceAdapter.save(ProductFixture.createProduct());

        List<ProductProfile> candidates = adapter.loadCandidates(emptySpec());

        assertThat(candidates).isEmpty();
    }

    private ProductSearchSpec emptySpec() {
        return new ProductSearchSpec(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                ProductSearchSpec.FallbackPolicy.RELAX_PREFERRED_KEEP_BLOCKED
        );
    }

    @TestConfiguration
    static class ObjectMapperTestConfig {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
