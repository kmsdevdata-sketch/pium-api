package com.pium.domain.product.fixture;

import com.pium.domain.product.enumtype.FunctionalLabel;
import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.UsageStep;
import com.pium.domain.product.model.Product;

import java.util.List;

public final class ProductFixture {

    private ProductFixture() {
    }

    public static Product createProduct() {
        return Product.create(
                "https://oliveyoung.example/products/1",
                "피움랩",
                "장벽 진정 크림",
                ProductCategory.LOTION_CREAM,
                UsageStep.MOISTURIZE,
                "https://image.example/product.png",
                "정제수, 글리세린, 판테놀, 세라마이드엔피",
                "장벽과 진정을 위한 크림",
                List.of(FunctionalLabel.BARRIER_FUNCTION_RECOVERY),
                "초기 등록 상품"
        );
    }
}
