# SkinNeedProfile

## 1. Purpose

`SkinNeedProfile`은 `SkinAnalysisResult`를 상품 추천이 소비할 수 있는 중간 언어로 번역한 모델이다.

피움은 사용자의 피부를 단순 타입으로 분류하지 않는다.  
건조, 유분, 트러블, 민감, 장벽, 색소, 노화 신호가 서로 겹쳐 있는 상태로 해석한다.

따라서 추천 엔진은 `DRYNESS HIGH -> 보습 추천` 같은 단일 축 매핑만 사용하면 안 된다.  
추천 전 단계에서 중첩 상태를 다음 정보로 해석해야 한다.

- 필요한 care need
- 피해야 할 risk
- 현재 피부의 risk tolerance
- 강한 조합 패턴
- goal이 반영될 수 있는 범위

---

## 2. Position

```text
SkinAnalysisResult
-> SkinNeedProfile
-> RecommendationPolicy
```

`SkinAnalysisResult`는 분석 결과의 사실 데이터이고,  
`SkinNeedProfile`은 추천을 위한 해석 데이터다.

`RecommendationPolicy`는 `SkinNeedProfile`을 입력으로 받아 ProductProfile과 비교한다.

---

## 3. Core Fields

```json
{
  "resultId": "sar_123",
  "profileVersion": "skin-need-v1",
  "riskTolerance": "CAUTION",
  "needWeights": {
    "HYDRATION_SUPPORT": 0.72,
    "BARRIER_SUPPORT": 0.88,
    "SOOTHING_SUPPORT": 0.64,
    "SEBUM_BALANCE": 0.79,
    "BLEMISH_SUPPORT": 0.41,
    "BRIGHTENING_SUPPORT": 0.2,
    "ANTI_AGING_SUPPORT": 0.35
  },
  "avoidWeights": {
    "HIGH_IRRITATION": 0.8,
    "FRAGRANCE_OR_ALLERGEN": 0.7,
    "HEAVY_OCCLUSIVE": 0.6,
    "STRONG_EXFOLIATION": 0.75
  },
  "safetyCaps": {
    "ACTIVE_INTENSITY_RISK": "LOW",
    "EXFOLIATION_EFFECT": "LOW"
  },
  "compositePatterns": [
    "BARRIER_WITH_OILINESS",
    "HYDRATION_WITH_SEBUM_BALANCE"
  ],
  "careStrategy": "장벽 부담과 유분 신호가 함께 있어 가벼운 장벽 보조와 유수분 균형을 함께 보는 전략"
}
```

---

## 4. Risk Tolerance

`riskTolerance`는 추천 상품이 감당할 수 있는 위험 강도를 뜻한다.

| 값 | 의미 |
| --- | --- |
| SAFE_ONLY | 장벽과 민감 신호가 모두 강해 고자극 후보를 강하게 제한 |
| CAUTION | 장벽 또는 민감 신호가 있어 위험 후보에 상한선/감점 적용 |
| BALANCED | 안전 신호가 상대적으로 낮아 benefit matching 중심으로 정렬 |

중요한 점은 `SAFE_ONLY`나 `CAUTION`이 장벽 제품만 추천한다는 뜻이 아니라는 것이다.

이 모드는 다음을 의미한다.

- 위험 성분/강한 액티브의 추천 가능성을 제한한다.
- 안전 기준을 통과한 상품 안에서 사용자의 주요 고민과 GOAL을 반영한다.
- 트러블, 색소, 탄력 고민을 무시하지 않고 안전한 강도로 다룬다.

---

## 5. Need Weights

`needWeights`는 사용자의 상태가 어떤 상품 trait를 필요로 하는지 표현한다.

예시 매핑:

| SkinMetric | LOW | MID | HIGH |
| --- | ---: | ---: | ---: |
| DRYNESS -> HYDRATION_SUPPORT | 0.1 | 0.6 | 1.0 |
| DRYNESS -> BARRIER_SUPPORT | 0.0 | 0.3 | 0.6 |
| BARRIER -> BARRIER_SUPPORT | 0.2 | 0.7 | 1.0 |
| SENSITIVITY -> SOOTHING_SUPPORT | 0.2 | 0.7 | 1.0 |
| OILINESS -> SEBUM_BALANCE | 0.1 | 0.6 | 1.0 |
| BLEMISH -> BLEMISH_SUPPORT | 0.1 | 0.6 | 1.0 |
| PIGMENTATION -> BRIGHTENING_SUPPORT | 0.1 | 0.6 | 1.0 |
| AGING -> ANTI_AGING_SUPPORT | 0.1 | 0.6 | 1.0 |

수치는 정책 버전에서 조정할 수 있다.

---

## 6. Composite Pattern

모든 조합을 if문으로 만들지 않는다.

모든 축은 기본 weight로 반영하고,  
의미가 강한 조합만 composite pattern으로 보정한다.

| Pattern | 해석 | 정책 방향 |
| --- | --- | --- |
| DRYNESS_WITH_OILINESS | 수분 부족과 유분 신호가 함께 있음 | 가벼운 수분 공급, heavy occlusive 주의 |
| BARRIER_WITH_OILINESS | 장벽 부담이 있지만 무거운 장벽 제품만 맞지 않을 수 있음 | lightweight barrier support 선호 |
| BARRIER_WITH_BLEMISH | 트러블 케어가 필요하지만 장벽 허용도가 낮음 | mild blemish care, soothing, barrier 우선 |
| SENSITIVITY_WITH_BLEMISH | 트러블 신호와 자극 반응이 함께 있음 | 강한 BHA/retinoid 지연, 진정형 트러블 케어 |
| BLEMISH_WITH_PIGMENTATION | 트러블 흔적이 톤 고민과 연결될 수 있음 | 선케어, 저자극 브라이트닝, 트러블 반복 완화 |
| AGING_WITH_BARRIER | 탄력 고민은 있지만 기능성 도입 전 장벽 안정 필요 | peptide/adenosine/보습 중심, strong active 지연 |
| PIGMENTATION_WITH_SENSITIVITY | 톤 고민은 있지만 자극 허용도가 낮음 | 선케어, 저자극 브라이트닝, 강한 비타민C/필링 주의 |

---

## 7. Safety Caps

이전 추천 문제의 핵심은 safety가 단순 감점으로만 작동해  
강한 benefit 점수가 위험 제품을 상위로 끌어올릴 수 있다는 점이다.

`safetyCaps`는 이 문제를 막기 위한 상한선이다.

예:

```text
BARRIER HIGH + BLEMISH HIGH
-> BLEMISH_SUPPORT 필요
-> 하지만 ACTIVE_INTENSITY_RISK는 LOW까지만 허용
-> EXFOLIATION_EFFECT HIGH 상품은 상위 추천 불가
```

정책 결과:

| 상품 | Benefit | Risk | 결과 |
| --- | --- | --- | --- |
| 강한 BHA 세럼 | BLEMISH HIGH | IRRITATION HIGH | 지연 또는 제외 |
| 진정 트러블 앰플 | BLEMISH MID, SOOTHING HIGH | IRRITATION LOW | 우선 추천 |

---

## 8. GOAL Handling

GOAL은 상태를 덮어쓰지 않는다.

GOAL은 다음 조건 안에서만 작동한다.

- safety cap을 통과한 상품 안에서 순서를 약하게 조정한다.
- primary need와 완전히 충돌하지 않는 경우에만 boost한다.
- `SAFE_ONLY`에서는 공격적 기능성 GOAL boost를 제한한다.

예:

```text
AGING goal + BARRIER HIGH
-> retinol boost 제한
-> low irritation anti-aging 또는 barrier-first 상품 우선
```

