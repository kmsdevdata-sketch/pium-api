package com.pium.adapter.inbound.web.admin.product;

import com.pium.adapter.inbound.response.ApiResponse;
import com.pium.application.product.dto.ProductCommand;
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
import com.pium.domain.product.enumtype.ProductCategory;
import com.pium.domain.product.enumtype.ProductStatus;
import com.pium.domain.product.vo.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 어드민 상품 관리 API.
 * - 추천/프로파일링 이전의 상품 원본 데이터를 등록하고 조회한다.
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final RegisterProduct registerProduct;
    private final UpdateProduct updateProduct;
    private final GetProduct getProduct;
    private final ListProducts listProducts;
    private final GenerateProductProfile generateProductProfile;
    private final GetProductProfile getProductProfile;

    @PostMapping
    public ApiResponse<ProductResponse> register(
            @RequestBody ProductRequest.Register request
    ) {
        ProductView response = registerProduct.register(request.toCommand());
        return ApiResponse.ok(ProductResponse.from(response));
    }

    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> update(
            @PathVariable String productId,
            @RequestBody ProductRequest.Update request
    ) {
        ProductView response = updateProduct.update(ProductId.of(productId), request.toCommand());
        return ApiResponse.ok(ProductResponse.from(response));
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> get(
            @PathVariable String productId
    ) {
        ProductView response = getProduct.get(ProductId.of(productId));
        return ApiResponse.ok(ProductResponse.from(response));
    }

    @GetMapping
    public ApiResponse<ProductListResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword
    ) {
        ProductListView response = listProducts.list(new ProductCommand.Search(
                status == null ? null : ProductStatus.of(status),
                category == null ? null : ProductCategory.of(category),
                keyword
        ));
        return ApiResponse.ok(ProductListResponse.from(response));
    }

    @PostMapping("/{productId}/profile")
    public ApiResponse<ProductProfileGenerationResponse> generateProfile(
            @PathVariable String productId
    ) {
        ProductProfileGenerationView response = generateProductProfile.generate(ProductId.of(productId));
        return ApiResponse.ok(ProductProfileGenerationResponse.from(response));
    }

    @GetMapping("/{productId}/profile")
    public ApiResponse<ProductProfileResponse> getProfile(
            @PathVariable String productId
    ) {
        ProductProfileView response = getProductProfile.get(ProductId.of(productId));
        return ApiResponse.ok(ProductProfileResponse.from(response));
    }
}
