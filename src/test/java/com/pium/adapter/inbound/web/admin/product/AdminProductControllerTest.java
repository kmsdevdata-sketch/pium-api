package com.pium.adapter.inbound.web.admin.product;

import com.pium.adapter.outbound.auth.jwt.JwtProperties;
import com.pium.application.auth.fixture.AuthFixture;
import com.pium.application.auth.required.LoadAuthenticatedUserPort;
import com.pium.application.product.dto.ProductListView;
import com.pium.application.product.dto.ProductView;
import com.pium.application.product.provided.GetProduct;
import com.pium.application.product.provided.ListProducts;
import com.pium.application.product.provided.RegisterProduct;
import com.pium.application.product.provided.UpdateProduct;
import com.pium.application.productprofile.dto.ProductProfileGenerationView;
import com.pium.application.productprofile.dto.ProductProfileView;
import com.pium.application.productprofile.provided.GenerateProductProfile;
import com.pium.application.productprofile.provided.GetProductProfile;
import com.pium.domain.product.enumtype.FunctionalLabel;
import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.ProductStatus;
import com.pium.domain.product.enumtype.UsageStep;
import com.pium.domain.product.vo.ProductId;
import com.pium.domain.productprofile.enumtype.ActiveFamily;
import com.pium.domain.productprofile.enumtype.EvidenceConfidence;
import com.pium.domain.productprofile.enumtype.EvidenceSourceField;
import com.pium.domain.productprofile.enumtype.EvidenceType;
import com.pium.domain.productprofile.enumtype.ProductIngredientGroup;
import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.productprofile.enumtype.TraitStrength;
import com.pium.domain.user.vo.UserId;
import com.pium.config.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminProductController.class)
@Import(SecurityConfig.class)
class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterProduct registerProduct;

    @MockitoBean
    private UpdateProduct updateProduct;

    @MockitoBean
    private GetProduct getProduct;

    @MockitoBean
    private ListProducts listProducts;

    @MockitoBean
    private GenerateProductProfile generateProductProfile;

    @MockitoBean
    private GetProductProfile getProductProfile;

    @MockitoBean
    private JwtProperties jwtProperties;

    @MockitoBean
    private LoadAuthenticatedUserPort loadAuthenticatedUserPort;

    @Test
    void register_상품을_등록한다() throws Exception {
        given(registerProduct.register(any()))
                .willReturn(productView("product-001", ProductStatus.ACTIVE));

        mockMvc.perform(post("/api/v1/admin/products")
                        .with(user(AuthFixture.createAdminAuthenticatedUser(UserId.of("admin-user"))))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceUrl": "https://oliveyoung.example/products/1",
                                  "brandName": "피움랩",
                                  "productName": "장벽 진정 크림",
                                  "category": "LOTION_CREAM",
                                  "usageStep": "MOISTURIZE",
                                  "imageUrl": "https://image.example/product.png",
                                  "ingredientText": "정제수, 글리세린, 판테놀",
                                  "claims": "장벽 진정 크림",
                                  "functionalLabels": ["BARRIER_FUNCTION_RECOVERY"],
                                  "adminMemo": "메모"
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value("product-001"))
                .andExpect(jsonPath("$.data.category").value("LOTION_CREAM"))
                .andExpect(jsonPath("$.data.functionalLabels[0]").value("BARRIER_FUNCTION_RECOVERY"))
                .andExpect(jsonPath("$.data.createdAt").value("2026-05-17T06:12:41Z"));
    }

    @Test
    void update_상품을_수정한다() throws Exception {
        given(updateProduct.update(any(ProductId.class), any()))
                .willReturn(productView("product-001", ProductStatus.INACTIVE));

        mockMvc.perform(put("/api/v1/admin/products/product-001")
                        .with(user(AuthFixture.createAdminAuthenticatedUser(UserId.of("admin-user"))))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceUrl": "https://oliveyoung.example/products/1",
                                  "brandName": "피움랩",
                                  "productName": "장벽 진정 크림",
                                  "category": "LOTiON_CREAM",
                                  "usageStep": "MOISTURIZE",
                                  "imageUrl": null,
                                  "ingredientText": "정제수",
                                  "claims": "claim",
                                  "functionalLabels": ["BARRIER_FUNCTION_RECOVERY"],
                                  "status": "INACTIVE",
                                  "adminMemo": "메모"
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void get_상품을_조회한다() throws Exception {
        given(getProduct.get(ProductId.of("product-001")))
                .willReturn(productView("product-001", ProductStatus.ACTIVE));

        mockMvc.perform(get("/api/v1/admin/products/product-001")
                        .with(user(AuthFixture.createAdminAuthenticatedUser(UserId.of("admin-user"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("장벽 진정 크림"));
    }

    @Test
    void list_상품목록을_조회한다() throws Exception {
        given(listProducts.list(any()))
                .willReturn(new ProductListView(1, List.of(productView("product-001", ProductStatus.ACTIVE))));

        mockMvc.perform(get("/api/v1/admin/products")
                        .param("status", "ACTIVE")
                        .param("category", "LOTION_CREAM")
                        .param("keyword", "장벽")
                        .with(user(AuthFixture.createAdminAuthenticatedUser(UserId.of("admin-user"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.products[0].productId").value("product-001"));
    }

    @Test
    void list_일반사용자는_어드민_상품목록을_조회할수없다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products")
                        .with(user(AuthFixture.createAuthenticatedUser(UserId.of("user-001"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_403_001"));
    }

    @Test
    void generateProfile_상품프로파일을_생성한다() throws Exception {
        given(generateProductProfile.generate(ProductId.of("product-001")))
                .willReturn(new ProductProfileGenerationView("product-001", true));

        mockMvc.perform(post("/api/v1/admin/products/product-001/profile")
                        .with(user(AuthFixture.createAdminAuthenticatedUser(UserId.of("admin-user"))))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value("product-001"))
                .andExpect(jsonPath("$.data.generated").value(true));
    }

    @Test
    void getProfile_상품프로파일을_조회한다() throws Exception {
        given(getProductProfile.get(ProductId.of("product-001")))
                .willReturn(productProfileView("product-001"));

        mockMvc.perform(get("/api/v1/admin/products/product-001/profile")
                        .with(user(AuthFixture.createAdminAuthenticatedUser(UserId.of("admin-user"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value("product-001"))
                .andExpect(jsonPath("$.data.benefitTraits[0].trait").value("BARRIER_SUPPORT"))
                .andExpect(jsonPath("$.data.riskTraits[0].trait").value("FRAGRANCE_OR_ALLERGEN_RISK"))
                .andExpect(jsonPath("$.data.ingredientGroups[0]").value("BARRIER_LIPID"))
                .andExpect(jsonPath("$.data.activeFamilies[0]").value("CERAMIDE"))
                .andExpect(jsonPath("$.data.evidenceSignals[0].id").value("ev_1"));
    }

    private ProductView productView(String productId, ProductStatus status) {
        return new ProductView(
                productId,
                "https://oliveyoung.example/products/1",
                "피움랩",
                "장벽 진정 크림",
                ProductCategory.LOTION_CREAM,
                UsageStep.MOISTURIZE,
                "https://image.example/product.png",
                "정제수, 글리세린, 판테놀",
                "장벽 진정 크림",
                List.of(FunctionalLabel.BARRIER_FUNCTION_RECOVERY),
                status,
                "메모",
                LocalDateTime.of(2026, 5, 17, 15, 12, 41),
                LocalDateTime.of(2026, 5, 17, 15, 12, 41)
        );
    }

    private ProductProfileView productProfileView(String productId) {
        return new ProductProfileView(
                productId,
                ProductCategory.LOTION_CREAM,
                UsageStep.MOISTURIZE,
                List.of(new ProductProfileView.BenefitTraitView(
                        RecommendationTrait.BARRIER_SUPPORT,
                        TraitStrength.MODERATE,
                        EvidenceConfidence.MEDIUM,
                        List.of("ev_1")
                )),
                List.of(new ProductProfileView.RiskTraitView(
                        ProductRiskTrait.FRAGRANCE_OR_ALLERGEN_RISK,
                        TraitStrength.WEAK,
                        EvidenceConfidence.LOW,
                        List.of("ev_2")
                )),
                List.of(ProductIngredientGroup.BARRIER_LIPID),
                List.of(ActiveFamily.CERAMIDE),
                List.of(
                        new ProductProfileView.EvidenceSignalView(
                                "ev_1",
                                EvidenceType.INGREDIENT_PRESENT,
                                EvidenceSourceField.INGREDIENTS,
                                "세라마이드 계열 성분이 포함되어 있음",
                                EvidenceConfidence.MEDIUM
                        ),
                        new ProductProfileView.EvidenceSignalView(
                                "ev_2",
                                EvidenceType.INGREDIENT_PRESENT,
                                EvidenceSourceField.INGREDIENTS,
                                "향료 계열 성분이 포함되어 있음",
                                EvidenceConfidence.LOW
                        )
                ),
                List.of("정확한 함량은 알 수 없음")
        );
    }
}
