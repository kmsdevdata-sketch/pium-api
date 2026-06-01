package com.pium.adapter.outbound.recommendation.productprofile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pium.adapter.outbound.productprofile.persistence.repository.ProductProfileJpaRepository;
import com.pium.application.recommendation.required.LoadRecommendationProductProfilePort;
import com.pium.domain.productprofile.model.ProductProfile;
import com.pium.domain.recommendation.model.search.ProductSearchSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 추천 계산에 사용할 ProductProfile 후보를 조회하는 어댑터
 * <p> 현재는 초기단계여서(상품수도 적어서) DB에서 직접 필터링하지 않음
 */
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecommendationProductProfileQueryAdapter implements LoadRecommendationProductProfilePort {

    private final ProductProfileJpaRepository productProfileJpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<ProductProfile> loadCandidates(ProductSearchSpec spec) {
        return productProfileJpaRepository.findAllActiveProfiles().stream()
                .map(entity -> entity.toDomain(objectMapper))
                .toList();
    }
}
