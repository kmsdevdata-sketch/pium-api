package com.pium.application.recommendation.service;

import com.pium.application.product.required.LoadProductPort;
import com.pium.application.recommendation.dto.ProductRecommendationDetailView;
import com.pium.application.recommendation.dto.ProductRecommendationItemView;
import com.pium.application.recommendation.dto.ProductRecommendationListView;
import com.pium.application.recommendation.exception.RecommendationApplicationErrorCode;
import com.pium.application.recommendation.exception.RecommendationApplicationException;
import com.pium.application.recommendation.provided.GetProductRecommendation;
import com.pium.application.recommendation.required.LoadRecommendationProductProfilePort;
import com.pium.application.skinanalysis.exception.SurveyApplicationErrorCode;
import com.pium.application.skinanalysis.exception.SurveyApplicationException;
import com.pium.application.skinanalysis.result.service.SkinAnalysisResultViewComposer;
import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.UsageStep;
import com.pium.domain.product.model.Product;
import com.pium.domain.product.vo.ProductId;
import com.pium.domain.productprofile.model.ProductProfile;
import com.pium.domain.recommendation.engine.interpretation.SkinInterpreter;
import com.pium.domain.recommendation.engine.scoring.RecommendationPolicy;
import com.pium.domain.recommendation.engine.search.ProductSearchSpecGenerator;
import com.pium.domain.recommendation.model.interpretation.SkinInterpretation;
import com.pium.domain.recommendation.model.scoring.ScoredRecommendationCandidate;
import com.pium.domain.recommendation.model.search.ProductSearchSpec;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import com.pium.domain.user.vo.UserId;
import com.pium.application.skinanalysis.result.required.LoadSkinAnalysisResultPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetProductRecommendationService implements GetProductRecommendation {

    private static final int TOP_RECOMMENDATION_LIMIT = 3;

    private final LoadSkinAnalysisResultPort loadSkinAnalysisResultPort;
    private final LoadRecommendationProductProfilePort loadRecommendationProductProfilePort;
    private final LoadProductPort loadProductPort;
    private final SkinAnalysisResultViewComposer skinAnalysisResultViewComposer;

    private final SkinInterpreter skinInterpreter = new SkinInterpreter();
    private final ProductSearchSpecGenerator productSearchSpecGenerator = new ProductSearchSpecGenerator();
    private final RecommendationPolicy recommendationPolicy = new RecommendationPolicy();
    private final ProductRecommendationTextComposer textComposer = new ProductRecommendationTextComposer();

    @Override
    public ProductRecommendationListView getLatest(UserId userId, String category) {
        SkinAnalysisResult result = latestResult(userId);
        SkinInterpretation interpretation = skinInterpreter.interpret(result);
        ProductSearchSpec spec = productSearchSpecGenerator.generate(interpretation);
        ProductCategory selectedCategory = selectedCategory(category);

        List<ScoredProduct> scoredProducts = scoredProducts(spec).stream()
                .filter(scoredProduct -> selectedCategory == null || scoredProduct.product.getCategory() == selectedCategory)
                .toList();

        List<ProductRecommendationItemView> topRecommendations = scoredProducts.stream()
                .limit(TOP_RECOMMENDATION_LIMIT)
                .map(this::toItemView)
                .toList();

        List<ProductRecommendationItemView> recommendations = scoredProducts.stream()
                .skip(TOP_RECOMMENDATION_LIMIT)
                .map(this::toItemView)
                .toList();

        return new ProductRecommendationListView(
                result.getId().value(),
                new ProductRecommendationListView.BasedOnView(
                        result.getCreatedAt(),
                        skinAnalysisResultViewComposer.composeListItem(result).oneLiner()
                ),
                textComposer.summary(interpretation),
                ProductRecommendationTextComposer.AD_DISCLOSURE,
                new ProductRecommendationListView.FilterView(
                        selectedCategory == null ? "ALL" : selectedCategory.name(),
                        categoryFilters()
                ),
                topRecommendations,
                recommendations
        );
    }

    @Override
    public ProductRecommendationDetailView getDetail(UserId userId, String productId) {
        latestResult(userId);

        ScoredProduct scoredProduct = scoredProductsForLatest(userId).stream()
                .filter(candidate -> candidate.product.getId().equals(ProductId.of(productId)))
                .findFirst()
                .orElseThrow(() -> new RecommendationApplicationException(
                        RecommendationApplicationErrorCode.RECOMMENDED_PRODUCT_NOT_FOUND
                ));

        return new ProductRecommendationDetailView(
                scoredProduct.product.getId().value(),
                scoredProduct.product.getBrandName(),
                scoredProduct.product.getProductName(),
                scoredProduct.product.getPrice(),
                scoredProduct.product.getImageUrl(),
                scoredProduct.product.getSourceUrl(),
                scoredProduct.product.getCategory().name(),
                categoryLabel(scoredProduct.product.getCategory()),
                scoredProduct.product.getUsageStep().name(),
                usageStepLabel(scoredProduct.product.getUsageStep()),
                scoredProduct.candidate.scoreBand().name(),
                textComposer.scoreBandLabel(scoredProduct.candidate.scoreBand()),
                textComposer.reasonDetails(scoredProduct.candidate, scoredProduct.profile),
                textComposer.recommendationReasons(scoredProduct.candidate),
                textComposer.cautions(scoredProduct.candidate),
                textComposer.careTagViews(scoredProduct.candidate),
                textComposer.cautionPointViews(scoredProduct.candidate),
                ProductRecommendationTextComposer.AD_DISCLOSURE
        );
    }

    private SkinAnalysisResult latestResult(UserId userId) {
        return loadSkinAnalysisResultPort.loadLatest(userId)
                .orElseThrow(() -> new SurveyApplicationException(
                        SurveyApplicationErrorCode.SKIN_ANALYSIS_RESULT_NOT_FOUND
                ));
    }

    private List<ScoredProduct> scoredProductsForLatest(UserId userId) {
        SkinAnalysisResult result = latestResult(userId);
        SkinInterpretation interpretation = skinInterpreter.interpret(result);
        ProductSearchSpec spec = productSearchSpecGenerator.generate(interpretation);
        return scoredProducts(spec);
    }

    private List<ScoredProduct> scoredProducts(ProductSearchSpec spec) {
        List<ProductProfile> profiles = loadRecommendationProductProfilePort.loadCandidates(spec);
        List<ScoredRecommendationCandidate> scoredCandidates = recommendationPolicy.score(spec, profiles);

        return scoredCandidates.stream()
                .map(candidate -> toScoredProduct(candidate, profiles))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingInt((ScoredProduct scoredProduct) -> scoredProduct.candidate.score()).reversed())
                .map(new Ranker()::rank)
                .toList();
    }

    private Optional<ScoredProduct> toScoredProduct(
            ScoredRecommendationCandidate candidate,
            List<ProductProfile> profiles
    ) {
        Optional<Product> product = loadProductPort.findById(candidate.productId());
        Optional<ProductProfile> profile = profiles.stream()
                .filter(candidateProfile -> candidateProfile.productId().equals(candidate.productId()))
                .findFirst();

        if (product.isEmpty() || profile.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ScoredProduct(0, candidate, product.get(), profile.get()));
    }

    private ProductRecommendationItemView toItemView(ScoredProduct scoredProduct) {
        Product product = scoredProduct.product;
        return new ProductRecommendationItemView(
                scoredProduct.rank,
                product.getId().value(),
                product.getBrandName(),
                product.getProductName(),
                product.getPrice(),
                product.getImageUrl(),
                product.getCategory().name(),
                categoryLabel(product.getCategory()),
                product.getUsageStep().name(),
                usageStepLabel(product.getUsageStep()),
                scoredProduct.candidate.scoreBand().name(),
                textComposer.scoreBandLabel(scoredProduct.candidate.scoreBand()),
                textComposer.itemReason(scoredProduct.candidate),
                textComposer.careTags(scoredProduct.candidate),
                textComposer.cautionPoints(scoredProduct.candidate)
        );
    }

    private ProductCategory selectedCategory(String category) {
        if (category == null || category.isBlank() || "ALL".equalsIgnoreCase(category.trim())) {
            return null;
        }
        return ProductCategory.of(category);
    }

    private List<ProductRecommendationListView.CategoryFilterView> categoryFilters() {
        return List.of(
                new ProductRecommendationListView.CategoryFilterView("ALL", "전체"),
                new ProductRecommendationListView.CategoryFilterView(ProductCategory.TONER.name(), "토너"),
                new ProductRecommendationListView.CategoryFilterView(ProductCategory.ESSENCE_SERUM.name(), "세럼/에센스"),
                new ProductRecommendationListView.CategoryFilterView(ProductCategory.LOTION_CREAM.name(), "로션/크림"),
                new ProductRecommendationListView.CategoryFilterView(ProductCategory.SUN_CARE.name(), "선케어"),
                new ProductRecommendationListView.CategoryFilterView(ProductCategory.SPOT_CARE.name(), "스팟케어"),
                new ProductRecommendationListView.CategoryFilterView(ProductCategory.MASK_PACK.name(), "마스크팩")
        );
    }

    private String categoryLabel(ProductCategory category) {
        return switch (category) {
            case CLEANSER -> "클렌저";
            case TONER -> "토너";
            case ESSENCE_SERUM -> "세럼/에센스";
            case LOTION_CREAM -> "로션/크림";
            case SUN_CARE -> "선케어";
            case MASK_PACK -> "마스크팩";
            case EXFOLIATOR -> "각질 케어";
            case SPOT_CARE -> "스팟케어";
            case MIST -> "미스트";
            case OIL_BALM -> "오일/밤";
            case ETC -> "기타";
        };
    }

    private String usageStepLabel(UsageStep usageStep) {
        return switch (usageStep) {
            case CLEANSE -> "클렌징 단계";
            case PREP -> "피부결 정돈 단계";
            case TREAT -> "집중 케어 단계";
            case MOISTURIZE -> "보습 단계";
            case PROTECT -> "보호 단계";
            case SPECIAL -> "스페셜 케어";
            case ETC -> "기타";
        };
    }

    private record ScoredProduct(
            int rank,
            ScoredRecommendationCandidate candidate,
            Product product,
            ProductProfile profile
    ) {
    }

    private static class Ranker {

        private int rank = 1;

        private ScoredProduct rank(ScoredProduct scoredProduct) {
            return new ScoredProduct(
                    rank++,
                    scoredProduct.candidate,
                    scoredProduct.product,
                    scoredProduct.profile
            );
        }
    }
}
