## Domain Overview

### 1.OverView
이 프로젝트는 사용자 피부 상태를 분석하고 , 이를 기반으로 적절한 제품을 추천하는 것을 목표로 한다 

핵심 흐름은 다음과 같다
```text
사용자 입력(설문) -> 피부 상태 분석 -> 필요 성분군 도출 -> 제품과 매칭 -> 추천 겨로가 생성
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
---
#### Generic Domain
서비스 확장을 위한 부가 기능 도메인 
- Community
- Engagement
---
### 3.Core Domain
#### 3.1 [SkinAnalysis](./SkinAnalysisDomain.md)
**역할**
사용자의 입력 데이터를 기반으로 피부 상태를 해석하고, 필요한 성분군을 도출한다 

**책임**
- 설문 정의 및 관리(Survey)
- 사용자 응답 처리(SurveyAnswer)
- 피부 상태 모델링(SkinCondition)
- 필요 성분군 생성(RequiredIngredientGroup)
---
#### 3.2 [Recommendation](./RecommendationDomain.md)
**역할**
사용자의 필요 성분군과 제품 데이터를 비교하여 추천 결과를 생성한다 

**책임**
- 추천 요청 처리 (RecommendationRequest)
- 사용자 요구 성분군과 제품 성분군 비교
- 제품별 점수 계산 (ProductScore)
---
### 4.Supporting Domain
#### 4.1 [Product](./ProductDomain.md)
**역할**
제품 및 성분 데이터를 관리하고 추천에 필요한 기준 데이터를 제공한다 

**책임**
- 제품 정보 관리(Product)
- 성분 정보 관리(Ingredient)
- 성분군 분류(IngredientGroup)
- 제품별 성분군 점수 제공(ProductIngredientScore)

**특징**
- 제품 성분군 점수는 사전 계산된 데이터로 관리된다 
- 추천 로직은 포함하지 않는다 
---
### 4.2 [User](./UserDomain.md)

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

