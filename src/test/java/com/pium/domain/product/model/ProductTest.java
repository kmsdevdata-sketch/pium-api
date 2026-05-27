package com.pium.domain.product.model;

import com.pium.domain.product.enumtype.FunctionalLabel;
import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.ProductStatus;
import com.pium.domain.product.enumtype.UsageStep;
import com.pium.domain.product.exception.ProductException;
import com.pium.domain.product.fixture.ProductFixture;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    void 상품_생성_검증() {
        Product product = ProductFixture.createProduct();

        assertThat(product.getId()).isNotNull();
        assertThat(product.getSourceUrl()).isEqualTo("https://oliveyoung.example/products/1");
        assertThat(product.getBrandName()).isEqualTo("피움랩");
        assertThat(product.getProductName()).isEqualTo("장벽 진정 크림");
        assertThat(product.getCategory()).isEqualTo(ProductCategory.LOTION_CREAM);
        assertThat(product.getUsageStep()).isEqualTo(UsageStep.MOISTURIZE);
        assertThat(product.getPrice()).isEqualTo(24000);
        assertThat(product.getFunctionalLabels()).containsExactly(FunctionalLabel.BARRIER_FUNCTION_RECOVERY);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.getCreatedAt()).isEqualTo(product.getUpdatedAt());
    }

    @Test
    void 상품_복원_검증() {
        Product product = ProductFixture.createProduct();

        Product reconstituted = Product.reconstitute(
                product.getId(),
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
                product.getStatus(),
                product.getAdminMemo(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );

        assertThat(reconstituted.getId()).isEqualTo(product.getId());
        assertThat(reconstituted.getFunctionalLabels()).containsExactlyElementsOf(product.getFunctionalLabels());
        assertThat(reconstituted.getStatus()).isEqualTo(product.getStatus());
    }

    @Test
    void 상품_수정_검증() {
        Product product = ProductFixture.createProduct();
        LocalDateTime before = product.getUpdatedAt();

        product.update(
                "https://oliveyoung.example/products/2",
                "새브랜드",
                "수분 진정 세럼",
                ProductCategory.ESSENCE_SERUM,
                UsageStep.TREAT,
                18000,
                null,
                "정제수, 글리세린, 나이아신아마이드",
                "수분 공급 세럼",
                List.of(FunctionalLabel.BRIGHTENING, FunctionalLabel.BRIGHTENING),
                ProductStatus.INACTIVE,
                "수정 메모"
        );

        assertThat(product.getBrandName()).isEqualTo("새브랜드");
        assertThat(product.getProductName()).isEqualTo("수분 진정 세럼");
        assertThat(product.getCategory()).isEqualTo(ProductCategory.ESSENCE_SERUM);
        assertThat(product.getImageUrl()).isNull();
        assertThat(product.getFunctionalLabels()).containsExactly(FunctionalLabel.BRIGHTENING);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        assertThat(product.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void 필수값이_유효하지_않으면_예외가_발생한다() {
        assertThatThrownBy(() -> Product.create(
                " ",
                "브랜드",
                "상품",
                ProductCategory.TONER,
                UsageStep.PREP,
                1000,
                null,
                null,
                null,
                List.of(),
                null
        )).isInstanceOf(ProductException.class);

        assertThatThrownBy(() -> Product.create(
                "https://source.example",
                "브랜드",
                "상품",
                ProductCategory.TONER,
                UsageStep.PREP,
                -1,
                null,
                null,
                null,
                List.of(),
                null
        )).isInstanceOf(ProductException.class);
    }
}
