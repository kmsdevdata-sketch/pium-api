## Recommendation Domain

### 1. Overview
Recommendation 도메인은 사용자에게 적절한 제품을 추천하는 역할을 담당한다.

이 도메인은 SkinAnalysis에서 도출된 사용자 상태 벡터와  
Product Profiling 계층에서 생성한 제품 특성 벡터를 기반으로 비교를 수행하고,  
그 결과로 추천 목록을 생성한다.

이 도메인은 설문 분석이나 상품 원본 해석을 수행하지 않으며,  
오직 “중간 해석 모델 간 비교 및 매칭”에 집중한다.
---
### 2. Responsibility

- 추천 요청 처리
- SkinNeedProfile과 ProductProfile 비교
- 제품별 적합도/위험도 계산
- 안전성 게이팅, 안전 상한선, 감점 정책 적용
- 추천 결과 생성
---
### 3. Core Concepts
#### 3.1 RecommendationRequest
추천을 수행하기 위한 입력 모델

- 사용자 식별 정보 (UserId)
- 피부 상태 해석 정보 (SkinNeedProfile)
- 사용자 목표/선호 정보 (Goal / Preference)
- 안전 제약 정보 (Safety Constraint)
---
#### 3.2 ProductScore
사용자 요구와 제품 간의 매칭 결과

- ProductId
- 매칭 점수

특징:
- Recommendation 도메인 내부 계산 결과
- 정렬 및 필터링의 기준이 됨
---
#### 3.3 SafetyPolicy
사용자 상태에서 허용 가능한 상품 risk 강도를 판단하는 정책

- hard block
- score cap
- risk penalty
- delayed recommendation

특징:
- 안전성은 단순 감점이 아니라 추천 가능성의 상한선으로도 작동한다
- benefit score가 높아도 safety cap을 넘으면 상위 추천으로 올라올 수 없다
---
### 4. Domain Flow
```text
recommendation-flow
SkinNeedProfile + ProductProfile + Goal/Safety → 안전 상한선 → 적합도 계산 → 위험 보정 → 정렬 및 필터링 → 추천 결과 생성
```
---
### 5. Boundary

- SkinAnalysis의 내부 해석 로직을 알지 않는다
- Product 도메인의 내부 산출 로직을 알지 않는다
- 오직 다음 데이터만 사용한다:
    - SkinNeedProfile
    - ProductProfile
    - Goal / Safety 정보
---
### 6. Key Design Principle

- "상태 기반 비교"에 집중한다
- 단일 축이 아니라 중첩 상태 해석을 기반으로 비교한다
- 정확도보다 안전성(민감/장벽 리스크 제어)을 우선한다
- 고정 임계값/고정 계수는 문서에서 확정하지 않고 규칙 버전으로 관리한다
---
### 7. Notes

- 추천 알고리즘은 도메인 내부 정책으로 정의되며,  
  향후 변경 가능하도록 설계한다

- 필터링 조건(가격, 카테고리 등)은  
  Application 레이어 또는 별도 정책으로 분리 가능하다

- 상세 추천 흐름은 [Recommendation Flow](../recommendation/RecommendationFlow.md)를 기준으로 한다
