## Product Domain

### 1. Overview
Product 도메인은 제품 및 성분 정보를 관리하고,  
추천에 필요한 기준 데이터를 제공하는 역할을 담당한다.

이 도메인은 제품 자체의 정보와 성분 구조를 표현하며,  
추천 로직이나 사용자 상태에 대한 해석은 포함하지 않는다.
---
### 2. Responsibility

- 제품 정보 관리
- 상품 원본 데이터 관리
- 전성분 원문 관리
- 상품 이미지/링크/카테고리/판매 상태 관리
- 어드민을 통한 상품 데이터 검수 지원
---
### 3. Core Concepts
#### 3.1 Product
제품을 나타내는 모델

- ProductId
- 이름
- 브랜드
- 가격
- 카테고리
- 사용 단계
- 이미지
- 원본 상품 링크
- 기타 메타 정보
---
#### 3.2 ProductRawData
추천 해석 이전의 상품 원본 데이터

- 상품명/브랜드
- 카테고리/사용 단계
- 가격/이미지
- 원본 링크
- 전성분 원문
- 상세페이지 claim
- 기능성 표시
- 어드민 검수 메모

특징:
- Product 도메인은 원본 사실을 보존한다.
- 원본 데이터를 추천 의미로 해석하지 않는다.
- 추천을 위한 trait 추출은 Product Profiling 계층의 책임이다.
---
#### 3.3 ProductProfile
제품 원본 데이터를 추천 엔진이 읽을 수 있는 trait/evidence/risk 색인으로 변환한 프로파일

- ProductId
- Category / UsageStep
- Benefit Traits (예: hydration support, soothing support)
- Risk Traits (예: irritation risk, strong exfoliation effect)
- Ingredient Groups
- Active Families
- Evidence Signals
- Warnings

특징:
- 추천 도메인에서 상태 기반 비교에 사용하는 기준 데이터
- Product Aggregate의 본질 속성이 아니라 Product Profiling / ACL의 산출물이다
- 사용자 상태별 `suitableFor.*` 같은 적합성 필드는 저장하지 않는다
- 수치 스케일/계수/임계값은 Product가 아닌 Recommendation 정책에서 해석한다
- 원본 상품 데이터가 바뀌면 재생성될 수 있다
- MVP에서는 AI-assisted profiler가 초안을 만들고 서버 validator가 검증하는 방식을 우선한다
---
### 4. Domain Flow

```text
product-flow
어드민 상품 등록 → ProductRawData 저장 → Product Profiling → ProductProfile 저장/갱신
```
---
### 5. Boundary

- SkinAnalysis 도메인을 알지 않는다
- Recommendation 도메인을 알지 않는다
- 사용자 상태를 알지 않는다
- ProductProfile의 추천 점수나 사용자별 적합도는 알지 않는다
---
### 6. Key Design Principle

- “데이터 제공”에 집중한다
- 제품 영향 특성은 제공하되, 추천 점수 계산은 수행하지 않는다
- 추천 로직을 포함하지 않는다
---
### 7. Notes

- 제품 특성화 방식은 변경 가능하며, 도메인 외부 파이프라인에서 관리한다
- Product 도메인은 추천 정책의 가중치/임계값에 의존하지 않는다
- 초기 상품 입력은 올리브영 쇼핑큐레이터 등 외부 링크를 어드민이 입력하고, 이미지/전성분/claim을 검수하는 흐름을 우선한다
