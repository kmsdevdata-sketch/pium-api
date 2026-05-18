## SkinAnalysis Domain

### 1. Overview

SkinAnalysis 도메인은 사용자 입력 데이터를 기반으로 피부 상태를 해석하고,  
추천 계산에 사용할 수 있는 상태 벡터를 생성하는 역할을 담당한다.

이 도메인은 외부 제품 정보나 추천 로직을 알지 않으며,  
오직 “사용자 상태 해석”에만 집중한다.
---
### 2. Responsibility

- 설문 기반 입력 데이터 해석
- 사용자 피부 상태 모델링
- 상태 스냅샷 생성 및 이력 관리
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

- `metric` (현재 코드: DRYNESS,BARRIER, OILINESS, BLEMISH_PRONENESS, SENSITIVITY, PIGMENTATION_TONE, AGING_SIGNS)
- `score` (0~100)

특징:
- 사용자 상태의 “사실(Fact)”을 정량화한 결과
- 추천 이전 단계의 표준 상태 표현

---

#### 3.3 Derived Signals (Optional)
추천 품질 향상을 위한 파생 신호 (선택)

- 예: `barrierState`, `reactivityRisk`, `goalPriority`

특징:
- 상태 벡터를 보조적으로 해석한 신호
- Recommendation 도메인의 안전성 제어/설명에 활용 가능
- 고정 임계값/고정 계수는 문서에서 강제하지 않고 정책 버전으로 관리

---

#### 3.4 SkinAnalysisResult
SkinAnalysis 바운디드의 최종 결과 모델 (Aggregate)

- `id`
- `userId`
- `skinMetricScores`
- `rulesVersion`
- `createdAt`
- `updatedAt`

특징:
- 동일 입력 + 동일 `rulesVersion`에서 일관된 결과를 보장하는 스냅샷
- 추적 가능한 메타데이터를 통해 회귀 검증/운영 분석 가능
- 필요 시 설명 필드(`primaryReasons`, `safetyAdjustments`)를 포함할 수 있음  
  (초기에는 `reasonCodes` 고정 enum 강제보다 유연한 설명 슬롯 권장)

### 4. Domain Flow
```text
skin-analysis-flow
SkinInput(설문 응답 스냅샷) → SkinMetricScore[] → (Optional Derived Signals) → SkinAnalysisResult
```
---
### 5. Boundary
- Product 도메인을 알지 않는다
- Recommendation 도메인을 알지 않는다
- 외부 AI/분석 도구에 의존하지 않는다 (Port로 분리)
- 해당 도메인의 결과는 "상태 표현"과 "보조 신호" 제공까지만 책임진다 
---
### 6. Key Design Principle
- 상태(State)와 추천(Strategy)을 분리한다
- 입력(설문)과 출력(상태 벡터) 사이의 변환 책임은 이 도메인에만 존재한다
- 동일 입력 + 동일 rulesVersion이면 동일 결과를 보장하도록 설계한다 
---
### 7. Notes
- 향후 이미지 분석, AI 기반 분석이 추가되더라도  
  동일한 상태 표현(SkinMetricScore)을 생성하도록 확장한다

- 분석 방식이 변경되어도 Recommendation 도메인은 영향을 받지 않는다
### 8. Integration Notes
- Recommendation 도메인은 SkinAnalysis 결과를 입력으로 소비한다 
- 필요시 `SkinAnalysisCompleted` 도메인 이벤트를 발행할수 있다 
- 위 이벤트는 이벤트 상태 변경알림을 뜻한다 

### 9.Survey translate
1.외부 응답 포맷
* 프론트에서 넘어오는 원본 
* likert,checkbox,top2등 자유롭게 변함
2.입력 어댑터/정규화 계층 (완충)
* 원본을 내부 표준 데이터로 변환 
* ex.NormalizedAnswer { questionId,valueType,values[] }
* 여기서 형식 검증/변환 실패처리
3.도메인 엔진 입력 계약(불변)
* 엔진은 오직 정규화된 표준 계약만 받음
* 엔진은 UI형식을 몰라야함 


### 10. Current Implementation Status (2026-05-09)
- 구현 완료
  - Aggregate: `SkinAnalysisResult`
  - VO: `SkinAnalysisResultId`, `RulesVersion`, `SkinMetricScore`, `RequiredIngredient`
  - Enum: `SkinMetric`, `IngredientGroup`
  - Exception: `SkinAnalysisException`, `SkinAnalysisErrorCode`
- 테스트 완료
  - `SkinAnalysisResultTest`에서 생성/복원/필수 리스트 예외 및 VO 범위/blank 예외 검증
- 다음 단계
  - 설문 스키마 확정 후 `SkinInput` 계약 정의
  - State 기반 도메인 엔진 인터페이스(`SkinAnalysisEngine`) 및 버전 구현체 도입
  - `RequiredIngredient`는 하위 호환용 신호로 유지하되, 추천 핵심 입력은 상태 벡터로 전환
