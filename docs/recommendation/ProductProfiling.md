# Product Profiling

## 1. Purpose

`ProductProfile`은 상품 원본 데이터를 추천 엔진이 이해할 수 있는 의미 모델로 번역한 결과다.

상품 원본에는 이름, 브랜드, 카테고리, 전성분, 상세페이지 문구, 기능성 표시, 이미지 등이 포함된다.
추천 엔진은 이 원본 데이터를 직접 소비하지 않고, Product Profiling 계층이 생성한 trait/strength/evidence/confidence를 소비한다.

MVP의 ProductProfile 생성은 룰 기반 성분 사전을 완성하는 방식보다
AI-assisted profiler를 우선한다. 단, AI는 추천을 수행하지 않고 상품 원본을 구조화된 색인으로 번역하는 역할만 맡는다.

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
| imageUrl | 어드민에서 등록하거나 원본에서 가져온 이미지 |
| ingredientText | 전성분 원문 |
| claims | 상세페이지/상품 설명 문구 |
| functionalLabels | 기능성 표시 정보 |
| adminMemo | 운영 검수 메모 |

이미지는 사용자 노출을 위한 데이터이며, 초기 추천 판단의 근거로 사용하지 않는다.

AI profiler 입력은 운영자가 저장한 원본 데이터로 제한한다.

```text
brandName
productName
category
usageStep
functionalLabels
ingredientText
claims
```

브랜드명과 상품명은 식별과 문맥 보조로만 사용한다.
인터넷 검색으로 외부 상품 정보를 임의 수집하지 않고, 필요한 상품 설명은 어드민이 `claims`에 입력하거나 보정한다.

## 4. What We Can Know

| 소스 | 알 수 있는 것 | 한계 | 근거 수준 |
| --- | --- | --- | --- |
| 전성분 | 성분 존재, 성분군, 일부 리스크 후보 | 정확한 함량과 실제 효능 강도는 알 수 없음 | 중간 |
| 성분 순서 | 상위 성분의 상대적 비중 힌트 | 일정 함량 이하 성분 순서는 약한 추론 | 중하 |
| 기능성 표시 | 미백/주름/자외선 등 규제상 효능 축 | 치료 또는 개인 효과 보장 아님 | 높음 |
| 상세페이지 claim | 브랜드가 의도한 포지셔닝 | 마케팅 주장일 수 있음 | 낮음~중간 |
| 카테고리 | 사용 맥락, 후보 필터 힌트 | benefit을 단정할 수 없음 | 낮음 |
| 피부타입 표기 | 브랜드의 타깃 사용자 | 객관 검증이 약할 수 있음 | 낮음 |

ProductProfile은 확실한 사실과 추론을 분리해 저장해야 한다.

## 5. ProductProfile v1

```json
{
  "productId": "prod_123",
  "category": "LOTION_CREAM",
  "usageStep": "MOISTURIZE",
  "benefitTraits": [
    {
      "trait": "BARRIER_SUPPORT",
      "strength": "MODERATE",
      "confidence": "MEDIUM",
      "evidenceRefs": ["ev_1", "ev_2"]
    }
  ],
  "riskTraits": [
    {
      "trait": "FRAGRANCE_OR_ALLERGEN_RISK",
      "strength": "WEAK",
      "confidence": "MEDIUM",
      "evidenceRefs": ["ev_3"]
    }
  ],
  "ingredientGroups": [
    "HUMECTANT",
    "BARRIER_LIPID",
    "SOOTHING"
  ],
  "activeFamilies": [
    "CERAMIDE",
    "PANTHENOL"
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

필드 의미:

| 필드 | 의미 | 생성 주체 |
| --- | --- | --- |
| productId | 어떤 상품의 프로파일인지 식별하는 ID | 서버 |
| category | 상품 카테고리 | 서버 |
| usageStep | 루틴에서 사용하는 단계 | 서버 |
| benefitTraits | 상품이 줄 수 있는 긍정적 기능/효과 trait 목록 | AI profiler + validator |
| riskTraits | 특정 피부 상태에서 부담이 될 수 있는 risk trait 목록 | AI profiler + validator |
| ingredientGroups | 전성분에서 추출한 성분군 목록 | AI profiler + validator |
| activeFamilies | 상품에 포함된 주요 기능성/활성 성분 계열 | AI profiler + validator |
| evidenceSignals | benefit/risk/성분군 판단에 사용된 근거 목록 | AI profiler + validator |
| warnings | 함량, pH, 실제 사용감 등 알 수 없는 부분에 대한 주의 메시지 | AI profiler + validator |

MVP에서 제외하는 필드:

| 필드 | 제외 이유 |
| --- | --- |
| profileVersion | 현재 단계에서 버전별 추적/교체를 운영하지 않는다 |
| leaveOn | boolean으로 단정하기 어렵고 현재 추천 로직에 필수적이지 않다 |
| textureEstimate | 실제 사용감은 원본 텍스트만으로 안정적으로 알기 어렵다 |
| residueType | 일부 추론 가능하지만 현재 추천 판단에 필수적이지 않다 |
| activeIntensity | 함량/pH/농도를 모르면 강도 단정 위험이 있다 |
| suitableFor.* | 사용자 상태 의존 필드이므로 ProductProfile에 저장하지 않는다 |

`strength`는 상품 trait의 강도를 뜻하고, 사용자 적합도를 뜻하지 않는다.

| Strength | 의미 |
| --- | --- |
| STRONG | 기능성 라벨, 상위 성분, 명확한 성분군 조합 등 근거가 강함 |
| MODERATE | 성분 존재와 카테고리/claim이 함께 맞음 |
| WEAK | 성분 존재 또는 claim 등 단일 약한 근거 |

정확한 함량, pH, 실제 사용감은 알 수 없으므로 strength는 효능 보장이 아니라 추천용 추론 강도다.

`STRONG_ACTIVE_RISK`, `STRONG_EXFOLIATION_EFFECT`처럼 안전성에 영향을 주는 강한 신호는
별도 `activeIntensity` 값으로 추상화하지 않고 risk trait와 evidence로 표현한다.

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
| STRONG_ACTIVE_RISK | 강한 기능성 성분 부담 가능성 |
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

## 9. Active Families

`activeFamilies`는 추천 점수의 직접 입력이라기보다, risk/benefit 설명과 향후 정책 확장에 쓰는 상품 자체 속성이다.
사용자 상태별 적합성은 여기서 판단하지 않는다.

초기 후보:

| Family | 의미 |
| --- | --- |
| CERAMIDE | 세라마이드 계열 |
| PANTHENOL | 판테놀 계열 |
| NIACINAMIDE | 나이아신아마이드 |
| RETINOID_LIKE | 레티놀/레티날 등 레티노이드 유사 계열 |
| AHA | AHA 산 성분 계열 |
| BHA | BHA 산 성분 계열 |
| PHA | PHA 산 성분 계열 |
| LHA | LHA 산 성분 계열 |
| VITAMIN_C | 비타민 C 또는 유도체 계열 |
| PEPTIDE | 펩타이드 계열 |
| ADENOSINE | 아데노신 |
| UV_FILTER | 자외선 차단 필터 |
| FRAGRANCE | 향료/향 알레르겐 계열 |
| HEAVY_OIL_WAX | 무거운 오일/왁스/밤 계열 |

주의:

- active family 존재는 성분 존재 또는 claim에 근거한 색인이다.
- 함량, pH, 실제 자극 강도를 확정하지 않는다.
- 강한 활성 신호가 필요하면 risk trait로 별도 표현한다.

## 10. MVP Ingredient Groups

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
| HEAVY_OCCLUSIVE | HEAVY_OCCLUSIVE_RISK | heavy oil, wax, balm-like occlusive groups |

주의:

- 성분 존재는 사실이지만 함량은 보통 알 수 없다.
- 성분 순서는 strength 추론에만 사용한다.
- 기능성 라벨은 해당 효능축의 강한 evidence지만 치료 효과 보장은 아니다.

## 11. AI-assisted Profiler Policy

ProductProfile 생성은 다음 구조를 따른다.

```text
ProductRawData
-> AI ProductProfiler
-> Structured JSON ProductProfile draft
-> Server Validator
-> ProductProfile 저장
```

원칙:

- AI는 ProductProfile 초안을 생성한다.
- 추천 런타임은 AI에 의존하지 않고 룰 기반으로 동작한다.
- AI 출력은 JSON schema와 enum으로 제한한다.
- 서버 validator가 enum, evidenceRefs, confidence, safety proxy를 검증한다.
- 함량, pH, 고농도/저농도는 원문 또는 claim이 없으면 확정하지 않는다.
- AI가 인터넷 검색으로 외부 정보를 임의 보강하지 않는다.
- 모델 출력에 근거가 없는 trait는 저장하지 않는다.

서버 validator가 보정해야 하는 대표 케이스:

| 케이스 | 처리 |
| --- | --- |
| 허용 enum 외 값 생성 | 거부 또는 제거 |
| evidenceRefs 없는 trait | 거부 또는 confidence 낮춤 |
| 함량 claim 없는 고농도 단정 | 제거 |
| pH 정보 없는 산성/저pH 단정 | 제거 |
| 상품명만 근거로 강한 효능 단정 | 제거 또는 낮은 confidence 처리 |
| STRONG_ACTIVE_RISK/STRONG_EXFOLIATION_EFFECT 누락 가능성 | safety proxy로 보정 |

## 12. Admin Requirements

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

MVP에서는 ProductProfile 품질을 위해 별도 수동 입력 필드를 즉시 추가하지 않는다.
현재 입력하는 브랜드명, 상품명, 카테고리, 사용 단계, 기능성 라벨, 전성분 원문, 상세페이지 claim을 우선 활용한다.

향후 품질이 부족하면 다음 보조 태그만 추가 검토한다.

- texture: LIGHT / BALANCED / RICH
- fragranceFreeClaim
- oilFreeClaim
- lowIrritationClaim
- sensitiveSkinClaim

이 태그들도 사실이 아니라 claim 또는 운영 검수 근거로 저장한다.
