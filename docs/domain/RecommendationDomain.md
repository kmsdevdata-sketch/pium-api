## Recommendation Domain

### 1. Overview
Recommendation 도메인은 사용자에게 적절한 제품을 추천하는 역할을 담당한다.

이 도메인은 SkinAnalysis에서 도출된 필요 성분군과  
Product 도메인의 제품 성분 데이터를 기반으로 비교를 수행하고,  
그 결과로 추천 목록을 생성한다.

이 도메인은 “분석”을 수행하지 않으며,  
오직 “비교 및 매칭”에만 집중한다.
---
### 2. Responsibility

- 추천 요청 처리
- 사용자 요구 성분군과 제품 데이터 비교
- 제품별 점수 산출
- 추천 결과 생성
---
### 3. Core Concepts
#### 3.1 RecommendationRequest
추천을 수행하기 위한 입력 모델

- 사용자 식별 정보 (UserId)
- 필요 성분군 정보 (RequiredIngredientGroup)
---
#### 3.2 ProductScore
사용자 요구와 제품 간의 매칭 결과

- ProductId
- 매칭 점수

특징:
- Recommendation 도메인 내부 계산 결과
- 정렬 및 필터링의 기준이 됨
---
### 4. Domain Flow
```text
recommendation-flow
RequiredIngredientGroup + ProductIngredientScore → ProductScore 계산 → 정렬 및 필터링 → 추천 결과 생성
```
---
### 5. Boundary

- SkinAnalysis의 내부 모델(SkinCondition 등)을 알지 않는다
- Product 도메인의 내부 계산 로직을 알지 않는다
- 오직 다음 데이터만 사용한다:
    - RequiredIngredientGroup
    - ProductIngredientScore
---
### 6. Key Design Principle

- “비교”에만 집중한다
- 입력은 해석된 데이터만 받는다 (RequiredIngredientGroup)
- 제품은 계산된 데이터만 사용한다 (ProductIngredientScore)
---
### 7. Notes

- 추천 알고리즘은 도메인 내부 정책으로 정의되며,  
  향후 변경 가능하도록 설계한다

- 필터링 조건(가격, 카테고리 등)은  
  Application 레이어 또는 별도 정책으로 분리 가능하다