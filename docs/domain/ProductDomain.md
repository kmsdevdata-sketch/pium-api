## Product Domain

### 1. Overview
Product 도메인은 제품 및 성분 정보를 관리하고,  
추천에 필요한 기준 데이터를 제공하는 역할을 담당한다.

이 도메인은 제품 자체의 정보와 성분 구조를 표현하며,  
추천 로직이나 사용자 상태에 대한 해석은 포함하지 않는다.
---
### 2. Responsibility

- 제품 정보 관리
- 성분 정보 관리
- 성분군 분류 관리
- 제품별 성분군 점수 제공
---
### 3. Core Concepts
#### 3.1 Product
제품을 나타내는 모델

- ProductId
- 이름
- 브랜드
- 가격
- 기타 메타 정보
---
#### 3.2 IngredientGroup (ENUM)
성분을 기능별로 분류한 그룹

예:
- 보습
- 진정
- 미백
- 주름개선

특징:
- 도메인 전반에서 공통적으로 사용되는 기준 개념
---
#### 3.3 ProductIngredientGroupScore
제품이 각 성분군에 대해 가지는 점수

- ProductId
- IngredientGroup
- Score

특징:
- 사전 계산된 데이터
- 추천 도메인에서 사용되는 기준 값
- 계산 로직은 포함하지 않음
---
### 4. Domain Flow

```text
product-flow
제품 수집 → 성분 추출 → 성분군 매핑 → 점수 계산 → ProductIngredientGroupScore 저장
```
---
### 5. Boundary

- SkinAnalysis 도메인을 알지 않는다
- Recommendation 도메인을 알지 않는다
- 사용자 상태를 알지 않는다
---
### 6. Key Design Principle

- “데이터 제공”에 집중한다
- 계산은 외부에서 수행하고 결과만 보유한다
- 추천 로직을 포함하지 않는다
---
### 7. Notes

- 성분군 점수는 AI 또는 별도 로직을 통해 사전 계산된다
- 계산 방식은 변경 가능하며, 도메인 외부에서 관리한다
- Product 도메인은 해당 계산 방식에 의존하지 않는다