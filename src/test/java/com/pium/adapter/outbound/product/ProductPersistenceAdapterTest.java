package com.pium.adapter.outbound.product;

import com.pium.adapter.outbound.product.persistence.repository.ProductJpaRepository;
import com.pium.application.product.dto.ProductCommand;
import com.pium.domain.product.enumtype.FunctionalLabel;
import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.ProductStatus;
import com.pium.domain.product.fixture.ProductFixture;
import com.pium.domain.product.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(ProductPersistenceAdapter.class)
class ProductPersistenceAdapterTest {

    @Autowired
    private ProductPersistenceAdapter persistenceAdapter;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void save_상품과_기능성표시를_저장한다() {
        Product product = ProductFixture.createProduct();

        Product saved = persistenceAdapter.save(product);

        entityManager.flush();
        entityManager.clear();

        var entity = productJpaRepository.findById(saved.getId().value()).orElseThrow();

        assertThat(entity.getProductId()).isEqualTo(saved.getId().value());
        assertThat(entity.getBrandName()).isEqualTo("피움랩");
        assertThat(entity.getFunctionalLabels()).containsExactly(FunctionalLabel.BARRIER_FUNCTION_RECOVERY);
    }

    @Test
    void findById_상품을_도메인으로_복원한다() {
        Product product = persistenceAdapter.save(ProductFixture.createProduct());

        entityManager.flush();
        entityManager.clear();

        Product loaded = persistenceAdapter.findById(product.getId()).orElseThrow();

        assertThat(loaded.getId()).isEqualTo(product.getId());
        assertThat(loaded.getFunctionalLabels()).containsExactlyElementsOf(product.getFunctionalLabels());
    }

    @Test
    void search_status_category_keyword_조건으로_조회한다() {
        Product product = ProductFixture.createProduct();
        Product excluded = Product.create(
                "https://oliveyoung.example/products/other",
                "다른브랜드",
                "피지 세럼",
                ProductCategory.ESSENCE_SERUM,
                com.pium.domain.product.enumtype.UsageStep.TREAT,
                null,
                null,
                null,
                null,
                null
        );
        excluded.update(
                excluded.getSourceUrl(),
                excluded.getBrandName(),
                excluded.getProductName(),
                excluded.getCategory(),
                excluded.getUsageStep(),
                excluded.getImageUrl(),
                excluded.getIngredientText(),
                excluded.getClaims(),
                excluded.getFunctionalLabels(),
                ProductStatus.EXCLUDED,
                excluded.getAdminMemo()
        );

        persistenceAdapter.save(product);
        persistenceAdapter.save(excluded);

        entityManager.flush();
        entityManager.clear();

        var result = persistenceAdapter.search(new ProductCommand.Search(
                ProductStatus.ACTIVE,
                ProductCategory.LOTION_CREAM,
                "장벽"
        ));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).isEqualTo("장벽 진정 크림");
    }
}
