## Domain Overview

### 1.OverView
이 프로젝트는 사용자 피부 상태를 분석하고 , 이를 기반으로 적절한 제품을 추천하는 것을 목표로 한다 

핵심 흐름은 다음과 같다
```text
사용자 입력(설문)
-> 피부 상태 벡터 생성
-> 추천용 중간 해석(SkinNeedProfile)
-> 상품 원본 데이터 프로파일링(ProductProfile)
-> 안전성 게이팅/상한선
-> 추천 결과 생성
```

이 과정에서 도메인은 역할과 책임에 따라 명확하게 분리된다 

### 2.Domain Classification
#### Core Domain
서비스의 핵심 가치와 비즈니스 로직을 담당하는 도메인 
- SkinAnalysis
- Recommendation
---
#### Supporting Domain
핵심 도메인을 지원하는 데이터 및 기능을 담당하는 도메인 
- Product
- User
- Product Profiling / ACL
---
#### Generic Domain
서비스 확장을 위한 부가 기능 도메인 
- Community
- Engagement
---
### 3.Core Domain
#### 3.1 [SkinAnalysis](./SkinAnalysisDomain.md)
**역할**
사용자의 입력 데이터를 기반으로 피부 "상태(State)"를 구조화한다.

**책임**
- 설문 정의 및 관리(Survey)
- 사용자 응답 처리(SurveyAnswer)
- 피부 상태 벡터 모델링(SkinMetricScore)
- 규칙 버전 기반 상태 해석 일관성 보장
---
#### 3.2 [Recommendation](./RecommendationDomain.md)
**역할**
사용자의 중첩 상태 해석과 제품 특성 프로파일을 비교해 안전한 추천 결과를 생성한다.

**책임**
- 추천 요청 처리 (RecommendationRequest)
- SkinNeedProfile과 ProductProfile 적합도 계산
- 위험도 기반 게이팅/상한선/패널티 적용
- 제품별 점수 계산 및 정렬 (ProductScore)
---
### 4.Supporting Domain
#### 4.1 [Product](./ProductDomain.md)
**역할**
제품 원본 데이터를 관리하고 추천에 필요한 상품 기준 데이터를 제공한다.

**책임**
- 제품 정보 관리(Product)
- 원본 상품 링크/이미지/카테고리 관리
- 전성분 원문 관리
- 상세페이지 claim 및 기능성 표시 원문 관리
- 어드민 검수 정보 관리

**특징**
- Product는 추천 로직 자체를 수행하지 않는다.
- ProductProfile은 Product Aggregate의 본질 속성이 아니라 Product Profiling / ACL의 산출물로 본다.
- 추천 로직은 포함하지 않는다.
---
#### 4.2 Product Profiling / ACL
**역할**
상품 원본 데이터를 추천 엔진이 이해할 수 있는 의미 모델(ProductProfile)로 번역한다.

**책임**
- 성분 정규화
- 성분군 분류
- benefit/risk trait 추출
- evidence/confidence 계산
- ProductProfile 생성 및 재생성

**특징**
- Product 도메인의 원본 모델을 Recommendation 도메인으로 직접 노출하지 않는다.
- 상품 상세페이지 claim은 사실이 아니라 낮은 신뢰도의 근거로만 다룬다.
- 초기 상품 입력은 어드민이 올리브영 쇼핑큐레이터 등 외부 링크와 이미지를 등록하는 흐름을 우선한다.
---
### 4.3 [User](./UserDomain.md)

#### 역할
사용자를 식별하고, 서비스 이용에 필요한 최소한의 사용자 상태를 관리한다.

#### 책임
- 사용자 식별 정보 관리 (User)
- 외부 인증 제공자와의 연결 정보 관리 (OAuth2 기반 식별)
- 사용자 피부 프로필 저장 (SkinProfile)

#### 특징
- 인증(OAuth2, JWT)은 도메인의 책임이 아니다
- 도메인은 “누구인가”만 알고, “어떻게 인증했는가”는 알지 않는다
- 인증 및 토큰 처리는 Application / Infrastructure 레이어에서 담당한다
