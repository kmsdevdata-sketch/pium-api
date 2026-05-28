# Product Profiling

## 1. Purpose

`ProductProfile`은 상품 원본 데이터를 추천 엔진이 이해할 수 있는 의미 모델로 번역한 결과다.

상품 원본에는 이름, 브랜드, 카테고리, 전성분, 상세페이지 문구, 기능성 표시, 이미지 등이 포함된다.  
추천 엔진은 이 원본 데이터를 직접 소비하지 않고, Product Profiling 계층이 생성한 trait/strength/evidence/confidence를 소비한다.

ProductProfile은 사용자 피부 상태별 적합성 표가 아니다.

```text
저장하지 않음:
- suitableFor.DRYNESS_MID
- suitableFor.BARRIER_HIGH
- notSuitableFor.SENSITIVITY_HIGH
```

사용자 상태에 따른 적합성은 `ProductSearchSpec`과 `RecommendationPolicy`가 런타임에 판단한다.

## 2. Boundary

Product 도메인은 "상품이 무엇인가"를 관리한다.

Product Profiling 계층은 "이 상품이 추천 관점에서 어떤 의미를 갖는가"를 해석한다.

Recommendation 도메인은 "이 유저에게 지금 이 상품을 보여줘도 되는가"를 판단한다.

```text
Product Domain
-> Product Profiling / ACL
-> ProductProfile
-> ProductSearchSpec과 비교
-> Recommendation Domain
```

`ProductProfile`은 Product Aggregate의 본질 속성이 아니다.  
추천을 위한 해석 산출물이므로 별도 테이블 또는 read model로 저장하는 방향을 우선한다.

## 3. ProductRawData

초기 상품 입력은 어드민 기반으로 운영한다.

관리자는 올리브영 쇼핑큐레이터 또는 외부 상품 링크를 입력하고,  
상품 이미지와 원본 데이터를 검수/보정한다.

ProductRawData 후보 필드:

| 필드 | 설명 |
| --- | --- |
| productId | 내부 상품 식별자 |
| sourceUrl | 올리브영 쇼핑큐레이터 등 원본 링크 |
| brandName | 브랜드명 |
| productName | 상품명 |
| category | 운영자가 선택한 상품 카테고리 |
| usageStep | 클렌징, 보습, 선케어 등 사용 단계 |
| price | 가격 정보 |
| imageUrl | 어드민에서 등록하거나 원본에서 가져온 이미지 |
| ingredientText | 전성분 원문 |
| claims | 상세페이지/상품 설명 문구 |
| functionalLabels | 기능성 표시 정보 |
| adminMemo | 운영 검수 메모 |

이미지는 사용자 노출을 위한 데이터이며, 초기 추천 판단의 근거로 사용하지 않는다.

## 4. What We Can Know

| 소스 | 알 수 있는 것 | 한계 | 근거 수준 |
| --- | --- | --- | --- |
| 전성분 | 성분 존재, 성분군, 일부 리스크 후보 | 정확한 함량과 실제 효능 강도는 알 수 없음 | 중간 |
| 성분 순서 | 상위 성분의 상대적 비중 힌트 | 일정 함량 이하 성분 순서는 약한 추론 | 중하 |
| 기능성 표시 | 미백/주름/자외선 등 규제상 효능 축 | 치료 또는 개인 효과 보장 아님 | 높음 |
| 상세페이지 claim | 브랜드가 의도한 포지셔닝 | 마케팅 주장일 수 있음 | 낮음~중간 |
| 카테고리 | 사용 맥락, 잔류/세정 여부 | benefit을 단정할 수 없음 | 낮음 |
| 피부타입 표기 | 브랜드의 타깃 사용자 | 객관 검증이 약할 수 있음 | 낮음 |

ProductProfile은 확실한 사실과 추론을 분리해 저장해야 한다.

## 5. ProductProfile v1

```json
{
  "productId": "prod_123",
  "profileVersion": "product-profiler-v1",
  "category": "LOTION_CREAM",
  "usageStep": "MOISTURIZE",
  "leaveOn": true,
  "benefitTraits": [
    {
      "trait": "BARRIER_SUPPORT",
      "strength": "MODERATE",
      "confidence": "MEDIUM",
      "targetsSkinMetrics": ["BARRIER", "DRYNESS"],
      "evidenceRefs": ["ev_1", "ev_2"]
    }
  ],
  "riskTraits": [
    {
      "trait": "FRAGRANCE_OR_ALLERGEN_RISK",
      "strength": "LOW",
      "confidence": "MEDIUM",
      "evidenceRefs": ["ev_3"]
    }
  ],
  "ingredientGroups": [
    "HUMECTANT",
    "BARRIER_LIPID",
    "SOOTHING"
  ],
  "evidenceSignals": [
    {
      "id": "ev_1",
      "type": "INGREDIENT_PRESENT",
      "sourceField": "INGREDIENTS",
      "message": "Ceramide NP included",
      "confidence": "MEDIUM"
    },
    {
      "id": "ev_2",
      "type": "CATEGORY",
      "sourceField": "CATEGORY",
      "message": "Cream category supports moisturizing use",
      "confidence": "LOW"
    }
  ],
  "warnings": [
    "Ingredient concentrations are unknown",
    "Cosmetic recommendation only"
  ]
}
```

`strength`는 상품 trait의 강도를 뜻하고, 사용자 적합도를 뜻하지 않는다.

| Strength | 의미 |
| --- | --- |
| STRONG | 기능성 라벨, 상위 성분, 명확한 성분군 조합 등 근거가 강함 |
| MODERATE | 성분 존재와 카테고리/claim이 함께 맞음 |
| WEAK | 성분 존재 또는 claim 등 단일 약한 근거 |

정확한 함량, pH, 실제 사용감은 알 수 없으므로 strength는 효능 보장이 아니라 추천용 추론 강도다.

## 6. Benefit Traits

| Trait | 의미 |
| --- | --- |
| HYDRATION_SUPPORT | 수분 공급/유지 보조 |
| BARRIER_SUPPORT | 장벽 보조/보습막/지질 보조 |
| SOOTHING_SUPPORT | 진정/편안함 보조 |
| SEBUM_CONTROL_SUPPORT | 유분/피지 균형 보조 |
| BLEMISH_CARE_SUPPORT | 트러블성 피부 관리 보조 |
| BRIGHTENING_SUPPORT | 톤/색소 고민 보조 |
| ANTI_AGING_SUPPORT | 탄력/주름 고민 보조 |
| UV_PROTECTION | 자외선 차단 |
| EXFOLIATION_EFFECT | 각질 정돈/결 개선 보조 |

`EXFOLIATION_EFFECT`는 benefit이면서 동시에 risk로도 작동할 수 있다.

## 7. Risk Traits

| Trait | 의미 |
| --- | --- |
| IRRITATION_RISK | 자극 가능성 |
| HIGH_IRRITATION_RISK | 자극 가능성이 큰 후보 |
| FRAGRANCE_OR_ALLERGEN_RISK | 향료/알레르겐 가능성 |
| COMEDOGENIC_RISK | 모공 막힘 가능성 힌트 |
| DRYING_OR_STRIPPING_RISK | 건조/탈지 가능성 |
| STRONG_EXFOLIATION_EFFECT | 강한 각질/산 케어 가능성 |
| STRONG_ACTIVE_RISK | 강한 기능성 성분 강도 |
| HEAVY_OCCLUSIVE_RISK | 무겁거나 답답할 수 있는 밀폐감 |

Risk trait는 "나쁜 상품"이라는 뜻이 아니다.  
특정 피부 상태에서 추천 강도를 낮추거나 지연해야 하는 신호다.

## 8. Evidence Model

| Evidence Type | 신뢰도 | 설명 |
| --- | --- | --- |
| REGULATORY_LABEL | 높음 | 기능성 표시, SPF/PA 등 |
| INGREDIENT_PRESENT | 중간 | 전성분에 특정 성분 존재 |
| INGREDIENT_POSITION | 중하 | 성분 순서 기반 약한 추론 |
| INGREDIENT_GROUP | 중간 | 성분군 조합 기반 |
| CATEGORY | 낮음 | 카테고리 기반 약한 추론 |
| MARKETING_CLAIM | 낮음~중간 | 상세페이지 문구 |
| ADMIN_REVIEW | 중간 | 운영자가 검수한 보정 정보 |

추천 점수는 evidence confidence를 참고할 수 있지만,  
마케팅 claim만으로 강한 benefit을 만들지 않는다.

## 9. MVP Ingredient Groups

초기 성분군은 넓게 잡고, 실제 운영 데이터로 세분화한다.

| Group | 연결 trait | 예시 성분 |
| --- | --- | --- |
| HUMECTANT | HYDRATION_SUPPORT | glycerin, hyaluronic acid, sodium PCA, urea, panthenol |
| BARRIER_LIPID | BARRIER_SUPPORT | ceramide, cholesterol, fatty acids, squalane |
| SOOTHING | SOOTHING_SUPPORT | panthenol, allantoin, centella, madecassoside, beta-glucan |
| SEBUM_PORE | SEBUM_CONTROL_SUPPORT | niacinamide, zinc PCA, clay, salicylic acid |
| BLEMISH_CARE | BLEMISH_CARE_SUPPORT | salicylic acid, niacinamide, azelaic acid, sulfur |
| BRIGHTENING | BRIGHTENING_SUPPORT | niacinamide, vitamin C derivatives, arbutin |
| ANTI_AGING | ANTI_AGING_SUPPORT | adenosine, peptides, retinoid-like ingredients |
| UV_FILTER | UV_PROTECTION | zinc oxide, titanium dioxide, organic UV filters |
| FRAGRANCE_ALLERGEN | FRAGRANCE_OR_ALLERGEN_RISK | fragrance, parfum, limonene, linalool, citral |
| EXFOLIATING_ACID | EXFOLIATION_EFFECT / STRONG_EXFOLIATION_EFFECT | AHA, BHA, PHA, LHA |

주의:

- 성분 존재는 사실이지만 함량은 보통 알 수 없다.
- 성분 순서는 strength 추론에만 사용한다.
- 기능성 라벨은 해당 효능축의 강한 evidence지만 치료 효과 보장은 아니다.

## 10. Admin Requirements

어드민은 초기 상품 품질을 좌우하는 핵심 도구다.

필수 기능:

- 상품 링크 등록
- 상품 이미지 등록/수정
- 상품명/브랜드/카테고리/사용 단계 입력
- 전성분 원문 입력/수정
- 기능성 표시 선택
- 상세페이지 claim 입력
- 판매 상태 관리
- ProductProfile 재생성
- ProductProfile 결과 검수
- 추천 노출 제외 플래그

어드민에서 ProductProfile을 직접 수동 작성하기보다,  
Profiler 결과를 확인하고 필요한 원본 데이터 또는 운영 메모를 보정하는 흐름을 우선한다.

향후 품질이 부족하면 다음 보조 태그만 추가 검토한다.

- texture: LIGHT / BALANCED / RICH
- fragranceFreeClaim
- oilFreeClaim
- lowIrritationClaim
- sensitiveSkinClaim

이 태그들도 사실이 아니라 claim 또는 운영 검수 근거로 저장한다.
