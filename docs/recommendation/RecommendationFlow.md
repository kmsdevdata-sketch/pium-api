# Recommendation Flow

## 1. Purpose

이 문서는 피움의 상품 추천 흐름을 정의한다.

피움 추천의 목표는 사용자를 단순 피부 타입으로 분류하는 것이 아니라,  
`SkinAnalysisResult`에 담긴 중첩적인 피부 상태를 추천 가능한 중간 언어로 해석한 뒤  
상품의 의미 프로파일과 비교해 안전한 후보를 제시하는 것이다.

추천은 "이 상품이 피부를 개선한다"는 단정이 아니라,  
"현재 응답 기준으로 우선 고려할 만한 상품과 주의할 상품을 정렬한다"는 수준으로 운영한다.

---

## 2. High Level Flow

```text
설문 응답
-> SkinAnalysisResult
-> SkinNeedProfile
-> ProductRawData
-> ProductProfile
-> RecommendationPolicy
-> RecommendationResult
```

각 단계의 책임은 다음과 같다.

| 단계 | 책임 |
| --- | --- |
| SkinAnalysisResult | 7축 피부 상태 점수와 LOW/MID/HIGH 라벨의 기반 데이터 |
| SkinNeedProfile | 중첩 피부 상태를 추천 언어로 해석한 중간 모델 |
| ProductRawData | 어드민이 입력한 상품 원본 정보 |
| ProductProfile | 상품 원본을 추천 엔진이 이해할 수 있는 trait/evidence로 번역한 모델 |
| RecommendationPolicy | 안전 상한선, benefit matching, risk penalty, goal boost 적용 |
| RecommendationResult | 추천 상품, 추천 이유, 지연/주의 이유 |

---

## 3. Product Input Flow

상품 데이터는 초기에 자동 크롤링을 전제로 하지 않는다.

운영자가 어드민 페이지에서 올리브영 쇼핑큐레이터 또는 외부 상품 링크를 입력하고,  
상품 이미지와 필요한 원본 정보를 함께 등록하는 흐름을 우선 고려한다.

```text
Admin
-> Olive Young / Shopping Curator Link 입력
-> 상품명, 브랜드, 카테고리, 가격, 이미지 확인
-> 전성분/상세 설명/기능성 표시 입력 또는 보정
-> ProductRawData 저장
-> ProductProfiler 실행
-> ProductProfile 저장
```

어드민이 다루는 정보는 다음과 같다.

| 정보 | 사용처 | 주의 |
| --- | --- | --- |
| 상품 링크 | 원본 추적, 운영 검수 | 추천 엔진은 링크 자체를 해석하지 않음 |
| 상품 이미지 | 사용자 노출 | 추천 판단 근거로 사용하지 않음 |
| 상품명/브랜드 | 사용자 노출, 식별 | 효능 근거로 직접 사용하지 않음 |
| 카테고리 | 사용 단계/제형 추론 | 단독 benefit 근거로 과신하지 않음 |
| 전성분 | 성분군/리스크/benefit 추론 | 정확한 함량은 알 수 없음 |
| 상세페이지 문구 | 상품 포지셔닝 보조 | 마케팅 claim으로 낮은 신뢰도 적용 |
| 기능성 표시 | 미백/주름/자외선 등 강한 근거 | 치료 표현으로 해석하지 않음 |

---

## 4. Recommendation Runtime Flow

추천 요청 시 런타임 흐름은 다음과 같다.

```text
1. 사용자 최신 SkinAnalysisResult 조회
2. SkinNeedProfile 생성 또는 조회
3. 추천 가능한 ProductProfile 후보 조회
4. SafetyPolicy 적용
5. BenefitMatcher 적용
6. RiskPenalty 적용
7. GOAL 보조 가중치 적용
8. 카테고리 다양성 보정
9. RecommendationResult 생성
```

SafetyPolicy는 단순 감점이 아니라 추천 가능성의 상한선으로 동작한다.

```text
Pre-filter  : 명백히 위험한 후보 제외
Score cap   : 위험 강도가 높으면 최대 추천 점수 제한
Penalty     : 애매한 위험은 감점
Delay       : 지금은 미루는 상품으로 분류
Explain     : 왜 추천/지연/제외됐는지 문장화
```

---

## 5. Optional AI Flow

MVP의 기본 추천은 AI 없이 동작해야 한다.

AI는 기본 추천을 대체하지 않고, 사용자가 별도 진입한 경우  
기본 후보군의 의미 비교와 설명 강화를 돕는 선택형 계층으로 둔다.

```text
Basic Recommendation Top N
-> AI Semantic Rerank / Explanation
-> RecommendationPolicy Validator
-> Enhanced RecommendationResult
```

AI가 직접 전체 상품군에서 마음대로 추천하지 않도록 한다.

AI 입력은 다음 데이터로 제한한다.

- SkinNeedProfile
- ProductProfile 후보군
- 추천 정책상 반드시 지켜야 하는 safety constraints

AI 출력은 구조화된 recommendation candidate로 받고,  
서버의 policy validator가 최종 검증한다.

---

## 6. First MVP Scope

첫 구현 범위는 다음을 목표로 한다.

- 상품 원본 등록 어드민
- ProductRawData 저장
- ProductProfile v1 생성
- SkinNeedProfile v1 생성
- 룰 기반 basic recommendation
- 추천 이유/주의 이유 저장 또는 응답

초기에는 정밀 상품 처방이 아니라  
"피부 상태 기반의 안전한 상품 후보 정렬"을 목표로 한다.

