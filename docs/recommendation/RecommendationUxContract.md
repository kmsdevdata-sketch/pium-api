# Recommendation UX Contract

## 1. Purpose

이 문서는 추천 API가 프론트 화면에 제공해야 하는 데이터와 문구 원칙을 정의한다.

추천 화면은 내부 도메인 용어를 그대로 노출하지 않는다.
`trait`, `risk`, `score` 같은 표현은 사용자 화면에서 사용하지 않고,
케어 포인트와 사용 전 참고할 점으로 바꿔 보여준다.

## 2. Entry Flow

추천은 사용자의 최신 피부 진단 결과를 기준으로 생성한다.

```text
홈 최신 진단 카드
-> 맞춤 상품 추천 보기
-> GET /api/v1/users/me/recommendations?category=ALL
```

```text
진단 결과 화면 하단
-> 이 결과로 상품 추천 보기
-> 현재 MVP에서는 최신 진단 기준 추천으로 이동
```

향후 특정 진단 결과 기준 추천이 필요하면 다음 API를 검토한다.

```text
GET /api/v1/users/me/skin-analysis-results/{resultId}/recommendations
```

## 3. API

### Recommendation List

```text
GET /api/v1/users/me/recommendations?category=ALL
GET /api/v1/users/me/recommendations?category=ESSENCE_SERUM
```

응답은 최신 진단 기준 추천이다.
카테고리 필터는 서버에서 처리한다.
rank는 전체 추천 기준 rank를 유지한다.

응답 주요 필드:

```text
analysisResultId
basedOn.createdAt
basedOn.summary
recommendationSummary.headline
recommendationSummary.reasons
recommendationSummary.notices
adDisclosure
filters
topRecommendations
recommendations
```

### Recommendation Detail

```text
GET /api/v1/users/me/recommendations/{productId}
```

응답은 최신 진단 기준 추천 결과 안에서 해당 상품의 상세 추천 정보를 제공한다.

응답 주요 필드:

```text
productId
brandName
productName
price
imageUrl
sourceUrl
categoryLabel
usageStepLabel
scoreBandLabel
reasonDetails
recommendationReasons
cautions
careTags
cautionPoints
adDisclosure
```

## 4. Ad Disclosure

추천 메인 화면 상단과 상품 상세 CTA 근처에 광고 고지를 노출한다.

필수 문구:

```text
이 포스팅은 올리브영 쇼핑 큐레이터 활동의 일환으로, 구매 시 일정 금액의 수수료를 제공받습니다.
```

프론트는 이 문구를 흐리게 숨기거나 footnote처럼 과도하게 축소하지 않는다.
상단 안내 박스와 외부 링크 버튼 근처에 명확히 노출한다.

## 5. User-Facing Labels

내부 `RecommendationTrait`는 화면에서 `careTags`로 노출한다.

| 내부 값 | 화면 문구 |
| --- | --- |
| HYDRATION_SUPPORT | 수분 충전 |
| BARRIER_SUPPORT | 장벽 케어 |
| SOOTHING_SUPPORT | 진정 케어 |
| SEBUM_CONTROL_SUPPORT | 피지 밸런스 |
| BLEMISH_CARE_SUPPORT | 트러블 케어 |
| BRIGHTENING_SUPPORT | 톤 케어 |
| ANTI_AGING_SUPPORT | 탄력 케어 |
| UV_PROTECTION | 자외선 차단 |
| EXFOLIATION_EFFECT | 결 정돈 |

내부 `ProductRiskTrait`는 화면에서 `cautionPoints`로 노출한다.
화면에서 `risk`라는 단어는 사용하지 않는다.

| 내부 값 | 화면 문구 |
| --- | --- |
| IRRITATION_RISK | 자극 주의 |
| HIGH_IRRITATION_RISK | 높은 자극 주의 |
| FRAGRANCE_OR_ALLERGEN_RISK | 향 성분 주의 |
| COMEDOGENIC_RISK | 모공 부담 주의 |
| DRYING_OR_STRIPPING_RISK | 건조감 주의 |
| STRONG_EXFOLIATION_EFFECT | 강한 각질 케어 주의 |
| STRONG_ACTIVE_RISK | 고기능 성분 주의 |
| HEAVY_OCCLUSIVE_RISK | 무거운 사용감 주의 |

## 6. Reason Copy Policy

추천 이유는 결과 나열이 아니라 다음 구조를 따른다.

```text
사용자의 피부 상태
+ 상품에서 확인한 포인트
+ 추천에 반영한 방식
```

카드에서는 한 줄 요약만 보여준다.

예:

```text
건조 신호가 높아, 상품의 수분 케어 포인트가 확인된 후보를 우선 매칭했어요.
```

상세에서는 `reasonDetails`를 사용한다.

```text
진단에서 본 점
최근 진단에서 건조 신호가 높게 보여 수분을 먼저 채우는 방향이 필요해요.

상품에서 확인한 점
전성분에서 보습 성분군이 확인돼요.

추천에 반영한 방식
그래서 수분 충전 포인트는 추천에 반영하고, 사용 전 주의할 신호는 함께 확인했어요.
```

## 7. Evidence Limit

백엔드는 항상 다음 문장은 안정적으로 만들 수 있다.

- 진단 상태 기반 문장
- careTags 기반 추천 방향 문장
- cautionPoints 기반 주의 문장
- goal 기반 추천 방향 문장

성분명 또는 상세 근거 문장은 `ProductProfile.evidenceSignals`에 해당 근거가 있을 때만 구체화한다.
근거가 부족하면 일반 문장으로 대체한다.

```text
상품 프로파일에서 수분 충전 포인트가 확인됐어요.
```

프론트는 evidence 문장이 없거나 일반적이어도 레이아웃이 깨지지 않게 구성한다.

## 8. Copy Restrictions

사용 가능한 표현:

- 도움이 될 수 있어요
- 추천 방향과 잘 맞아요
- 천천히 사용해보세요
- 사용 전 참고해보세요
- 주의가 필요해요

피해야 할 표현:

- 치료
- 개선 보장
- 완치
- 효능 보장
- 반드시 좋아져요
- 의학적 처방처럼 보이는 표현

추천은 화장품 선택 참고용이며 의학적 진단이 아니라는 안내를 유지한다.
