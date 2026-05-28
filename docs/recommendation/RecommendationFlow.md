# Recommendation Flow

## 1. Purpose

이 문서는 피움의 상품 추천 흐름을 정의한다.

피움 추천의 목표는 사용자를 단순 피부 타입으로 분류하거나  
특정 HIGH 고민에 대응하는 상품을 바로 추천하는 것이 아니다.

추천은 다음 질문에 답해야 한다.

```text
현재 이 사용자의 피부 상태 전체를 고려했을 때,
어떤 조건을 가진 상품을 우선 보여줘야 안전하고 적합한가?
```

## 2. High Level Flow

```text
설문 응답
-> SkinAnalysisResult
-> SkinInterpretation
-> ProductSearchSpec
-> ProductProfile 후보 조회
-> RecommendationPolicy
-> RecommendationResult
```

각 단계의 책임은 다음과 같다.

| 단계 | 책임 |
| --- | --- |
| SkinAnalysisResult | 7축 피부 상태 점수/라벨과 최대 2개 goal 저장 |
| SkinInterpretation | LOW/MID/HIGH 조합을 피부 상태 의미로 해석 |
| ProductSearchSpec | 상품 후보를 필터/랭킹하기 위한 조건 생성 |
| ProductRawData | 어드민이 입력한 상품 원본 정보 |
| ProductProfile | 상품 원본을 trait/evidence/risk로 번역한 추천용 색인 |
| RecommendationPolicy | hard gate, benefit matching, goal boost, fallback 적용 |
| RecommendationResult | 추천 상품, 추천 이유, 주의/대안 이유 |

## 3. Product Input Flow

상품 데이터는 초기에 자동 크롤링을 전제로 하지 않는다.

운영자가 어드민 페이지에서 올리브영 쇼핑큐레이터 또는 외부 상품 링크를 입력하고,  
상품 이미지와 필요한 원본 정보를 함께 등록한다.

```text
Admin
-> Olive Young / Shopping Curator Link 입력
-> 상품명, 브랜드, 카테고리, 가격, 이미지 확인
-> 전성분/상세 설명/기능성 표시 입력 또는 보정
-> ProductRawData 저장
-> ProductProfiler 실행
-> ProductProfile 저장 또는 재생성
```

어드민이 다루는 정보:

| 정보 | 사용처 | 주의 |
| --- | --- | --- |
| 상품 링크 | 원본 추적, 운영 검수 | 추천 엔진은 링크 자체를 해석하지 않음 |
| 상품 이미지 | 사용자 노출 | 추천 판단 근거로 사용하지 않음 |
| 상품명/브랜드 | 사용자 노출, 식별 | 효능 근거로 직접 사용하지 않음 |
| 카테고리 | 사용 단계/제형 추론 | 단독 benefit 근거로 과신하지 않음 |
| 전성분 | 성분군/리스크/benefit 추론 | 정확한 함량은 알 수 없음 |
| 상세페이지 문구 | 상품 포지셔닝 보조 | 마케팅 claim으로 낮은 신뢰도 적용 |
| 기능성 표시 | 미백/주름/자외선 등 강한 근거 | 치료 표현으로 해석하지 않음 |

## 4. Runtime Flow

추천 요청 시 런타임 흐름은 다음과 같다.

```text
1. 사용자 최신 SkinAnalysisResult 조회
2. SkinAnalysisResult의 goals 검증
3. SkinInterpretation 생성
4. ProductSearchSpec 생성
5. 추천 가능한 ProductProfile 후보 조회
6. Hard Gate 적용
7. Required/Preferred/Goal trait 매칭
8. Risk Penalty와 Caution 문구 생성
9. 후보 부족 시 Fallback 적용
10. RecommendationResult 생성
```

추천은 처음에는 상품추천 요청 시 동기 계산한다.  
상품 수가 늘거나 계산 비용이 커지면 `resultId + goals + policyHash` 단위 캐시를 검토한다.

## 5. Goal Conflict Flow

GOAL은 추천 방향을 보정하지만 안전 규칙보다 우선하지 않는다.

```text
SkinInterpretation
-> goal과 현재 상태 충돌 여부 판단
-> ProductSearchSpec에서 goalBoost 강도 조절
-> RecommendationResult에 안내 문구 포함
```

예:

```text
BARRIER HIGH + goal Q11_2(트러블)
-> 트러블 케어 boost는 유지
-> strong exfoliation/high irritation은 block 또는 강한 penalty
-> 사용자에게 장벽 우선 트러블 케어 안내
```

## 6. Safety Flow

Safety는 단순 감점이 아니라 추천 가능성의 상한선으로 동작한다.
상세 케이스와 MVP proxy는 [Safety Policy](SafetyPolicy.md)를 기준으로 한다.

```text
Hard Block    : 현재 상태에서 추천 후보 제외
Soft Penalty  : 후보에는 남기되 점수 크게 감점
Caution       : 추천 가능하지만 주의 문구 생성
```

대표 케이스:

| 상태 | 상품 risk | 처리 |
| --- | --- | --- |
| BARRIER HIGH | STRONG_EXFOLIATION_EFFECT | Hard Block 또는 Soft Penalty |
| SENSITIVITY HIGH | FRAGRANCE_OR_ALLERGEN_RISK | Soft Penalty 또는 Caution |
| DRYNESS HIGH | DRYING_OR_STRIPPING_RISK | Soft Penalty |
| OILINESS MID/HIGH | HEAVY_OCCLUSIVE_RISK | Caution 또는 Soft Penalty |

## 7. RecommendationResult

추천 결과는 상품 목록만 반환하지 않는다.

필수 정보:

```text
productId
rank
scoreBand
matchedNeeds
matchedGoals
matchedTraits
riskWarnings
penaltyReasons
fallbackApplied
recommendationReason
policyVersion
profileVersion
```

`scoreBand`는 사용자에게 정밀 점수로 노출하기보다 내부 디버깅 또는 적합도 구간 표시용으로 사용한다.

## 8. Optional AI Flow

MVP의 기본 추천은 AI 없이 동작해야 한다.

AI는 기본 추천을 대체하지 않고, 사용자가 별도 진입한 경우  
기본 후보군의 의미 비교와 설명 강화를 돕는 선택형 계층으로 둔다.

```text
Basic Recommendation Candidates
-> AI Semantic Rerank / Explanation
-> RecommendationPolicy Validator
-> Enhanced RecommendationResult
```

AI가 직접 전체 상품군에서 마음대로 추천하지 않도록 한다.

AI 입력은 다음 데이터로 제한한다.

- SkinInterpretation
- ProductSearchSpec
- ProductProfile 후보군
- 반드시 지켜야 하는 safety constraints

AI 출력은 구조화된 recommendation candidate로 받고,  
서버의 policy validator가 최종 검증한다.

## 9. First MVP Scope

첫 구현 범위:

- 상품 원본 등록 어드민
- ProductRawData 저장
- ProductProfile v1 생성
- SkinInterpretation v1 생성
- ProductSearchSpec v1 생성
- 룰 기반 basic recommendation
- 추천 이유/주의/대안 이유 저장 또는 응답

초기에는 정밀 상품 처방이 아니라  
"피부 상태 기반의 안전한 상품 후보 정렬"을 목표로 한다.
