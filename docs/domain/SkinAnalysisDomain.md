## SkinAnalysis Domain

### 1. Overview

SkinAnalysis 도메인은 사용자 입력 데이터를 기반으로 피부 상태를 해석하고,  
추천에 필요한 성분군 정보를 도출하는 역할을 담당한다.

이 도메인은 외부 제품 정보나 추천 로직을 알지 않으며,  
오직 “사용자 상태 해석”에만 집중한다.
---
### 2. Responsibility

- 설문 기반 입력 데이터 정의 및 관리
- 사용자 응답 수집 및 해석
- 피부 상태 모델링
- 필요 성분군 도출
---
### 3. Core Concepts
#### 3.1 Survey
설문 구조를 정의하는 모델

- 질문 목록
- 선택지
- 점수 규칙 (도메인 내부 정책)
---
#### 3.2 SurveyAnswer
사용자의 설문 응답

- 사용자 선택 값
- 설문과의 매핑 관계
---
#### 3.3 SkinCondition
분석된 피부 상태를 나타내는 모델

예:
- 건조
- 피지
- 트러블
- 민감
- 색소
- 노화

특징:
- 정량적 값(점수) 기반 상태 표현
- 사용자 상태의 “사실”을 나타냄
---
#### 3.4 RequiredIngredientGroup
추천을 위해 도출된 필요 성분군

특징:
- SkinCondition을 기반으로 생성됨
- 추천 도메인으로 전달되는 결과 값
- 사용자 상태를 “추천 목적”으로 해석한 결과
---
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
---
### 6. Key Design Principle
- “상태”와 “해석 결과”를 분리한다
    - SkinCondition (상태)
    - RequiredIngredientGroup (추천용 해석)

- 입력(설문)과 출력(성분군) 사이의 변환 책임은 이 도메인에만 존재한다
---
### 7. Notes
- 향후 이미지 분석, AI 기반 분석이 추가되더라도  
  동일한 출력(RequiredIngredientGroup)을 생성하도록 확장한다

- 분석 방식이 변경되어도 Recommendation 도메인은 영향을 받지 않는다