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
- 제품 특성 프로파일 제공
- 안전성 관련 메타데이터 제공
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
#### 3.3 ProductProfile
제품이 피부 상태에 어떤 방향으로 작용하는지 표현하는 프로파일

- ProductId
- Benefit Traits (예: hydration support, calming support)
- Risk Traits (예: irritation risk, exfoliation strength)
- Compatibility Traits (예: barrier friendly, sensitive-skin suitability)

특징:
- 추천 도메인에서 상태 기반 비교에 사용하는 기준 데이터
- 수치 스케일/계수/임계값은 Product가 아닌 Recommendation 정책에서 해석
---
### 4. Domain Flow

```text
product-flow
제품 수집 → 성분 추출 → 특성 매핑 → ProductProfile 저장/갱신
```
---
### 5. Boundary

- SkinAnalysis 도메인을 알지 않는다
- Recommendation 도메인을 알지 않는다
- 사용자 상태를 알지 않는다
---
### 6. Key Design Principle

- “데이터 제공”에 집중한다
- 제품 영향 특성은 제공하되, 추천 점수 계산은 수행하지 않는다
- 추천 로직을 포함하지 않는다
---
### 7. Notes

- 제품 특성화 방식은 변경 가능하며, 도메인 외부 파이프라인에서 관리한다
- Product 도메인은 추천 정책의 가중치/임계값에 의존하지 않는다
