package com.pium.adapter.outbound.productprofile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pium.adapter.outbound.product.ProductPersistenceAdapter;
import com.pium.adapter.outbound.productprofile.persistence.repository.ProductProfileJpaRepository;
import com.pium.domain.product.fixture.ProductFixture;
import com.pium.domain.product.model.Product;
import com.pium.domain.productprofile.fixture.ProductProfileFixture;
import com.pium.domain.productprofile.model.ProductProfile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        ProductPersistenceAdapter.class,
        ProductProfilePersistenceAdapter.class,
        ProductProfilePersistenceAdapterTest.ObjectMapperTestConfig.class
})
class ProductProfilePersistenceAdapterTest {

    @Autowired
    private ProductPersistenceAdapter productPersistenceAdapter;

    @Autowired
    private ProductProfilePersistenceAdapter productProfilePersistenceAdapter;

    @Autowired
    private ProductProfileJpaRepository productProfileJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void save_ProductProfile을_JSON으로_저장한다() {
        Product product = productPersistenceAdapter.save(ProductFixture.createProduct());
        ProductProfile productProfile = ProductProfileFixture.createProductProfile(product.getId());

        ProductProfile saved = productProfilePersistenceAdapter.save(productProfile);

        entityManager.flush();
        entityManager.clear();

        var entity = productProfileJpaRepository.findById(product.getId().value()).orElseThrow();

        assertThat(saved.productId()).isEqualTo(product.getId());
        assertThat(entity.getProductId()).isEqualTo(product.getId().value());
        assertThat(entity.getProfileJson()).contains("BARRIER_SUPPORT");
    }

    @Test
    void findByProductId_ProductProfile을_도메인으로_복원한다() {
        Product product = productPersistenceAdapter.save(ProductFixture.createProduct());
        ProductProfile productProfile = ProductProfileFixture.createProductProfile(product.getId());
        productProfilePersistenceAdapter.save(productProfile);

        entityManager.flush();
        entityManager.clear();

        ProductProfile loaded = productProfilePersistenceAdapter.findByProductId(product.getId()).orElseThrow();

        assertThat(loaded.productId()).isEqualTo(product.getId());
        assertThat(loaded.benefitTraits().get(0).trait().name()).isEqualTo("BARRIER_SUPPORT");
        assertThat(loaded.evidenceSignals()).hasSize(2);
    }

    @Test
    void save_기존_ProductProfile을_갱신한다() {
        Product product = productPersistenceAdapter.save(ProductFixture.createProduct());
        ProductProfile firstProfile = ProductProfileFixture.createProductProfile(product.getId());
        ProductProfile secondProfile = ProductProfile.of(
                product.getId(),
                firstProfile.category(),
                firstProfile.usageStep(),
                firstProfile.benefitTraits(),
                firstProfile.riskTraits(),
                firstProfile.ingredientGroups(),
                firstProfile.activeFamilies(),
                firstProfile.evidenceSignals(),
                java.util.List.of("새 warning")
        );

        productProfilePersistenceAdapter.save(firstProfile);
        productProfilePersistenceAdapter.save(secondProfile);

        entityManager.flush();
        entityManager.clear();

        ProductProfile loaded = productProfilePersistenceAdapter.findByProductId(product.getId()).orElseThrow();

        assertThat(productProfileJpaRepository.findAll()).hasSize(1);
        assertThat(loaded.warnings()).containsExactly("새 warning");
    }

    @TestConfiguration
    static class ObjectMapperTestConfig {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
