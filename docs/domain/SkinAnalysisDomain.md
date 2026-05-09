## SkinAnalysis Domain

### 1. Overview

SkinAnalysis 도메인은 사용자 입력 데이터를 기반으로 피부 상태를 해석하고,  
추천에 필요한 성분군 정보를 도출하는 역할을 담당한다.

이 도메인은 외부 제품 정보나 추천 로직을 알지 않으며,  
오직 “사용자 상태 해석”에만 집중한다.
---
### 2. Responsibility

- 설문 기반 입력 데이터 해석
- 사용자 피부 상태 모델링
- 피부 상태 기반 필요 성분군(RequiredIngredientGroup) 도출
- 규칙 버전(rulesVersion) 기준의 일관된 결과 생성 
---
### 3. Core Concepts

#### 3.1 AnalysisInput
피부 해석에 사용되는 입력 스냅샷(설문 응답 포함)

- 사용자 입력 값 집합
- 입력 시각
- 입력 출처/버전(선택)

특징:
- Survey 정의 자체를 도메인 엔티티로 강제하지 않음
- 정적 설문/외부 입력 포맷을 수용하는 경계 모델

---

#### 3.2 SkinMetricScore
분석된 피부 상태 점수 모델 (VO)

- `metric` (예: DRYNESS, SEBUM, ACNE, SENSITIVITY, REDNESS, PIGMENTATION, BARRIER_WEAKNESS, PHOTOAGING)
- `score` (0~100)

특징:
- 사용자 상태의 “사실(Fact)”을 정량화한 결과
- 추천 이전 단계의 표준 상태 표현

---

#### 3.3 RequiredIngredient
추천을 위해 도출된 필요 성분군 신호 모델 (VO)

- `group` (예: HYDRATION, BARRIER_REPAIR, SEBUM_CONTROL, ACNE_CARE, SOOTHING, BRIGHTENING, TURNOVER, ANTI_AGING, PHOTOPROTECTION, GENTLE_CLEANSING)
- `weight` (0~100)

특징:
- `SkinMetricScore`를 기반으로 생성
- Recommendation 도메인으로 전달되는 표준 입력 값
- “무엇이 필요한가”를 표현하며 “어떤 상품을 추천할지”는 포함하지 않음

---

#### 3.4 SkinAnalysisResult
SkinAnalysis 바운디드의 최종 결과 모델 (Aggregate)

- `userId`
- `skinMetricScores`
- `requiredIngredients`
- `rulesVersion`
- `analyzedAt`

특징:
- 동일 입력 + 동일 `rulesVersion`에서 일관된 결과를 보장하는 스냅샷
- 추적 가능한 메타데이터를 통해 회귀 검증/운영 분석 가능
- 필요 시 설명 필드(`primaryReasons`, `safetyAdjustments`)를 포함할 수 있음  
  (초기에는 `reasonCodes` 고정 enum 강제보다 유연한 설명 슬롯 권장)

### 4. Domain Flow
```text
skin-analysis-flow 
Survey → SurveyAnswer → SkinCondition → RequiredIngredientGroup
```
---
### 5. Boundary
- Product 도메인을 알지 않는다
- Recommendation 도메인을 알지 않는다
- 외부 AI/분석 도구에 의존하지 않는다 (Port로 분리)
- 해당 도메인의 결과는 "추천 계산용 입력 신호" 까지만 책임진다 
---
### 6. Key Design Principle
- “상태”와 “해석 결과”를 분리한다
    - SkinCondition (상태)
    - RequiredIngredientGroup (추천용 해석)

- 입력(설문)과 출력(성분군) 사이의 변환 책임은 이 도메인에만 존재한다
- 동일 입력 + 동일 rulesVersion이면 동일 결과를 보장하도록 설계한다 
---
### 7. Notes
- 향후 이미지 분석, AI 기반 분석이 추가되더라도  
  동일한 출력(RequiredIngredientGroup)을 생성하도록 확장한다

- 분석 방식이 변경되어도 Recommendation 도메인은 영향을 받지 않는다
### 8. Integration Notes
- Recommendation 도메인은 SkinAnalysis 결과를 입력으로 소비한다 
- 필요시 `SkinAnalysisCompleted` 도메인 이벤트를 발행할수 있다 
- 위 이벤트는 이벤트 상태 변경알림을 뜻한다 