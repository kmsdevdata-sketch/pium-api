# Frontend Recommendation Prompt

피움 사용자용 상품 추천 화면을 구현해줘.

## 전제

- 사용자는 이미 피부 진단을 완료한 상태다.
- 추천은 최신 피부 진단 결과를 기준으로 제공된다.
- 추천 상품은 올리브영 쇼핑 큐레이터 링크로 외부 이동할 수 있다.
- 광고 고지 문구를 추천 메인 화면과 상품 상세 CTA 근처에 반드시 노출한다.

필수 광고 고지:

```text
이 포스팅은 올리브영 쇼핑 큐레이터 활동의 일환으로, 구매 시 일정 금액의 수수료를 제공받습니다.
```

## API

목록:

```text
GET /api/v1/users/me/recommendations?category=ALL
GET /api/v1/users/me/recommendations?category=ESSENCE_SERUM
```

상세:

```text
GET /api/v1/users/me/recommendations/{productId}
```

서버 API가 준비되기 전에는 같은 타입의 mock 데이터로 먼저 구현한다.

## 메인 화면

구성 순서:

```text
Top Header
AdDisclosureBox
RecommendationSummarySection
TopRecommendationSection
CategoryFilter
RecommendationListSection
```

헤더:

```text
나에게 맞는 상품 추천
가장 최근 피부 진단 결과를 기준으로 추천했어요.
```

광고 고지:

- 상단 안내 박스로 명확히 노출한다.
- 문구를 흐리게 숨기거나 지나치게 작게 표시하지 않는다.

추천 기준 요약:

- `recommendationSummary.headline`
- `recommendationSummary.reasons`
- `recommendationSummary.notices`

Top 3:

- `topRecommendations`를 세로 강조 카드로 보여준다.
- 가로 캐러셀보다 세로 카드가 우선이다.
- 카드에는 이미지, 브랜드, 상품명, scoreBandLabel, careTags, cautionPoints, recommendationReason을 보여준다.

카테고리:

- `filters.categories`를 가로 스크롤 칩으로 보여준다.
- 기본은 `ALL`.
- 선택 시 API를 다시 호출한다.
- 서버는 전체 추천 rank를 유지하므로 필터링 후에도 rank 숫자를 그대로 표시할 수 있다.

전체 추천:

- `recommendations`를 compact list로 보여준다.
- Top 3보다 작은 카드로 구성한다.

## 상세 화면

구성 순서:

```text
Top Header
Product Image
Product Basic Info
Score Band
Reason Details
Cautions
Care Tags
Caution Points
AdDisclosureBox
Fixed Bottom CTA
```

CTA:

```text
올리브영에서 보기
```

외부 이동은 환경별 wrapper를 사용한다.

```text
Toss WebView: Toss 외부 URL 열기 API 우선
일반 웹: window.open(url, "_blank")
```

## 문구 원칙

화면에서 `trait`, `risk`라는 단어를 그대로 노출하지 않는다.

대신:

```text
trait -> 케어 포인트 / 잘 맞는 이유 / careTags
risk -> 사용 전 참고할 점 / cautionPoints
```

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

## 화면에서 사용할 주요 필드

메인:

```text
analysisResultId
basedOn.createdAt
basedOn.summary
recommendationSummary
adDisclosure
filters
topRecommendations
recommendations
```

상품 카드:

```text
rank
productId
brandName
productName
imageUrl
categoryLabel
usageStepLabel
scoreBandLabel
recommendationReason
careTags
cautionPoints
```

상세:

```text
productId
brandName
productName
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

## 추가 고지

백엔드는 성분명까지 항상 안정적으로 만들 수 있는 것은 아니다.
`reasonDetails`의 상품 근거는 ProductProfile evidence가 있을 때 더 구체적이고,
근거가 부족한 경우 일반적인 케어 포인트 문장으로 내려올 수 있다.
