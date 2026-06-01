# Recommendation Scoring Policy

## 1. Purpose

이 문서는 `RecommendationPolicy`의 초기 점수 상수와 운영 중 조정 기준을 기록한다.

현재 점수는 통계 학습 결과가 아니라 MVP 도메인 정책값이다.
목표는 정밀 예측보다 다음 우선순위를 안정적으로 표현하는 것이다.

```text
required need match
> preferred need match
> goal boost
> category hint
> caution penalty
> soft penalty
```

Safety는 점수보다 우선한다.
`blockedRiskTraits`에 걸린 상품은 점수를 계산하지 않고 후보에서 제외한다.

## 2. Initial Scores

| 항목 | 값 | 의미 |
| --- | ---: | --- |
| REQUIRED_TRAIT_SCORE | +40 | 현재 피부 상태상 우선 필요한 benefit trait가 상품에 있을 때 |
| PREFERRED_TRAIT_SCORE | +20 | 있으면 좋은 보조 benefit trait가 상품에 있을 때 |
| GOAL_MEDIUM_SCORE | +15 | 사용자 goal과 강하게 연결된 trait가 상품에 있을 때 |
| GOAL_LOW_SCORE | +8 | 사용자 goal과 보조로 연결된 trait가 상품에 있을 때 |
| PENALTY_RISK_SCORE | -25 | 후보에는 남기지만 상위 노출을 제한해야 하는 risk가 있을 때 |
| CAUTION_RISK_SCORE | -8 | 추천은 가능하지만 주의가 필요한 risk가 있을 때 |
| CATEGORY_HINT_SCORE | +5 | 현재 추천 방향과 상품 카테고리가 맞을 때 |

초기값의 의도:

- Required 1개는 preferred 2개와 비슷한 영향력을 가진다.
- Goal boost는 피부 상태 need를 뒤집지 못한다.
- Soft penalty 1개는 preferred 1개보다 강하게 작동한다.
- Caution은 상품을 크게 밀어내지 않고 설명/약한 감점 역할을 한다.
- Category hint는 동점 또는 근소한 점수 차이를 정리하는 보조 신호다.

## 3. Score Band

| Band | 기준 | 의미 |
| --- | ---: | --- |
| HIGH | 70 이상 | 주요 need를 잘 맞추고 risk 부담이 낮은 후보 |
| MEDIUM | 40 이상 | 핵심 need 일부를 충족하거나 보조 조건이 좋은 후보 |
| LOW | 40 미만 | 추천 가능하지만 적합 신호가 약한 후보 |

`score`는 0 미만으로 내려가지 않게 보정한다.

## 4. Expected Effects

### REQUIRED_TRAIT_SCORE

값을 올리면:

- 현재 피부 상태와 직접 연결된 상품이 더 강하게 상위로 올라온다.
- 후보가 적은 상태에서는 추천 결과가 특정 trait 상품으로 좁아질 수 있다.

값을 내리면:

- goal이나 category가 더 쉽게 순위를 뒤집는다.
- 현재 피부 상태와 덜 관련된 상품이 상위에 섞일 수 있다.

### PREFERRED_TRAIT_SCORE

값을 올리면:

- 여러 보조 trait를 가진 범용 상품이 상위로 올라온다.
- required가 약한 사용자에게 결과가 풍부해진다.

값을 내리면:

- 단일 핵심 need 중심으로 결과가 단순해진다.
- MID 상태 반영이 약해질 수 있다.

### GOAL_MEDIUM_SCORE / GOAL_LOW_SCORE

값을 올리면:

- 사용자가 고른 goal이 추천 순위에 더 크게 반영된다.
- 피부 상태와 충돌하는 goal이 과하게 올라올 수 있으므로 safety penalty와 함께 봐야 한다.

값을 내리면:

- 추천이 피부 상태 중심으로 안정된다.
- 사용자가 선택한 목표가 결과에 덜 보일 수 있다.

### PENALTY_RISK_SCORE

값을 더 음수로 내리면:

- risk가 있는 상품이 상위에서 강하게 밀린다.
- 민감/장벽 사용자에게 더 보수적인 결과가 나온다.

값을 0에 가깝게 올리면:

- benefit이 높은 risk 상품이 상위에 남기 쉬워진다.
- 자극/향료/강한 active 관련 불만이 늘 수 있다.

### CAUTION_RISK_SCORE

값을 더 음수로 내리면:

- 주의 수준 risk도 순위에 더 크게 반영된다.
- 후보가 안전하게 보이지만 너무 보수적일 수 있다.

값을 0에 가깝게 올리면:

- caution은 설명 중심으로 작동하고 순위 영향은 작아진다.
- risk warning이 붙은 상품이 상위에 자주 보일 수 있다.

### CATEGORY_HINT_SCORE

값을 올리면:

- 추천 방향별 카테고리 편향이 강해진다.
- 좋은 trait를 가진 다른 카테고리 상품이 밀릴 수 있다.

값을 내리면:

- category는 거의 tie-breaker로만 작동한다.
- 추천 결과가 사용 맥락과 덜 맞아 보일 수 있다.

## 5. Operation Scenarios

| 현상 | 우선 확인 | 조정 후보 |
| --- | --- | --- |
| 추천 상품이 너무 많이 비슷함 | required trait가 과하게 강한지 확인 | REQUIRED_TRAIT_SCORE 하향, PREFERRED_TRAIT_SCORE 상향 |
| 추천 상품이 너무 적거나 비어 보임 | hard block이 과한지, required가 너무 많은지 확인 | ProductSearchSpec fallback, blocked risk 정책, REQUIRED_TRAIT_SCORE |
| 관련 없는 상품이 상위에 나옴 | category/goal이 need를 뒤집는지 확인 | GOAL_MEDIUM_SCORE 하향, CATEGORY_HINT_SCORE 하향, REQUIRED_TRAIT_SCORE 상향 |
| 사용자의 goal이 결과에 잘 안 보임 | goal trait 매칭률과 boost 점수 확인 | GOAL_MEDIUM_SCORE 상향, GOAL_LOW_SCORE 상향 |
| 민감/장벽 사용자에게 자극 가능 상품이 높게 나옴 | penalty/caution 적용 여부 확인 | PENALTY_RISK_SCORE 더 음수로 조정, CAUTION_RISK_SCORE 더 음수로 조정 |
| 안전하지만 너무 평범한 상품만 나옴 | risk penalty가 과한지 확인 | PENALTY_RISK_SCORE 0에 가깝게 조정, PREFERRED_TRAIT_SCORE 상향 |
| 특정 카테고리만 반복됨 | category hint 영향 확인 | CATEGORY_HINT_SCORE 하향, category diversity 정책 추가 |

## 6. Adjustment Rule

운영 중에는 한 번에 여러 상수를 바꾸지 않는다.

추천 결과를 비교할 때는 다음 입력을 고정하고 하나의 상수만 바꿔본다.

```text
SkinAnalysisResult
goals
ProductProfile 후보군
ProductSearchSpec
```

그리고 다음 항목을 비교한다.

- 상위 5개 상품의 trait/risk 구성
- blocked 후보 수
- penalty/caution이 붙은 후보 수
- goal trait가 포함된 후보 비율
- 카테고리 분포
