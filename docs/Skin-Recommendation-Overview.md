# 스킨 추천 설계 개요 (MVP)

## 1. 목적

피움 추천은 "피부 고민 HIGH에 대응하는 상품 추천"이 아니다.

목표는 사용자의 전체 피부 상태와 개선 목표를 함께 해석해서,  
현재 피부가 감당할 수 있는 상품 조건을 만들고 그 조건으로 상품을 필터링/정렬하는 것이다.

## 2. 설계 원칙

- 사용자를 단순 피부 타입으로 분류하지 않는다.
- 7개 상태축의 LOW/MID/HIGH 조합을 해석한다.
- HIGH뿐 아니라 MID도 추천 조건으로 사용한다.
- GOAL은 방향키로 쓰되, 안전성 규칙을 뒤집지 않는다.
- ProductProfile은 사용자 상태별 suitableFor가 아니라 상품의 benefit/risk/evidence 색인으로 유지한다.
- 추천 결과에는 점수뿐 아니라 이유와 주의/완화 여부를 남긴다.

## 3. 현재 추천 흐름

```text
설문 응답
-> SkinAnalysisResult
   - 7개 SkinMetric score/level
   - UserGoal, 최대 2개
-> SkinInterpretation
   - 상태 조합 해석
   - goal 충돌 안내
   - routineIntent 결정
-> ProductSearchSpec
   - required/preferred/blocked/caution trait
   - goal boost
   - fallback policy
-> ProductProfile 후보 조회
-> RecommendationPolicy
   - hard gate
   - benefit matching
   - risk penalty
   - diversity/fallback
-> RecommendationResult
```

상세 흐름은 [Recommendation Flow](recommendation/RecommendationFlow.md)를 기준으로 한다.

## 4. SkinInterpretation

`SkinAnalysisResult`는 진단 결과의 사실 데이터다.  
추천 엔진은 이를 바로 상품과 비교하지 않고 먼저 `SkinInterpretation`으로 해석한다.

예:

```text
DRYNESS MID
BARRIER MID
OILINESS MID
SENSITIVITY LOW
BLEMISH LOW

-> 강한 교정 루틴보다 가벼운 수분 보충, 장벽 유지, 무거운 유분감 회피가 적합한 상태
```

이 단계에서 생성하는 것:

- 현재 피부 상태 요약 타입
- 주요/보조 니즈
- 주의해야 할 risk
- routineIntent
- goal 반영 가능 범위
- goal 충돌 안내

상세 내용은 [Skin Interpretation](recommendation/SkinInterpretation.md)을 기준으로 한다.

## 5. ProductSearchSpec

`ProductSearchSpec`은 SkinInterpretation을 상품 조회/필터/랭킹 가능한 조건으로 바꾼 것이다.

예:

```text
preferredTraits:
- HYDRATION_SUPPORT
- BARRIER_SUPPORT

cautionRiskTraits:
- HEAVY_OCCLUSIVE_RISK
- DRYING_OR_STRIPPING_RISK

categoryHints:
- TONER
- ESSENCE_SERUM
- LOTION_CREAM
```

즉 추천은 "전체 상품에 점수를 다 매긴 뒤 상위만 고르는 방식"이 아니라,  
먼저 현재 피부상태에 맞는 검색 조건을 만든 뒤 후보를 좁히고 정렬한다.

## 6. Goal 반영

현재 GOAL은 설문상 최대 2개까지 선택된다.

| Code | Goal |
| --- | --- |
| Q11_1 | 보습·수분감 |
| Q11_2 | 트러블·여드름 |
| Q11_3 | 피부 톤·미백 |
| Q11_4 | 모공·피지 |
| Q11_5 | 탄력·주름 |
| Q11_6 | 민감한 피부 진정·장벽 강화 |

원칙:

- GOAL은 상태를 덮어쓰지 않는다.
- GOAL은 안전 조건을 통과한 후보 안에서 우선순위를 조정한다.
- GOAL과 현재 상태가 충돌하면 모드를 나누지 않고 안내한다.

예:

```text
BARRIER HIGH + GOAL 트러블
-> 트러블 케어 goal은 반영
-> 강한 산/필링/고자극 액티브는 제한
-> 장벽을 해치지 않는 트러블 케어 상품 우선
```

## 7. Safety 우선순위

안전성은 단순 감점이 아니라 후보 가능성의 상한선이다.
상세 matrix와 관측 가능한 proxy는 [Safety Policy](recommendation/SafetyPolicy.md)를 기준으로 한다.

```text
1. Hard Block
2. Soft Penalty
3. Caution
```

예:

```text
BARRIER HIGH + BLEMISH HIGH
-> BARRIER/SOOTHING 우선
-> BLEMISH_CARE는 mild하게 반영
-> STRONG_EXFOLIATION/HIGH_IRRITATION 후보는 제외 또는 강한 감점
```

이 방식은 "장벽 문제가 있는데 강한 트러블 제품이 상위 추천되는 문제"를 줄이기 위한 핵심 장치다.

## 8. ProductProfile 원칙

상품 원본 데이터는 추천 엔진이 직접 소비하지 않는다.

```text
ProductRawData
-> ProductProfiling / ACL
-> ProductProfile
```

ProductProfile은 다음을 가진다.

- benefitTraits
- riskTraits
- safetyTraits
- traitStrength
- confidence
- evidenceSource
- evidenceChain

저장하지 않는 것:

```text
suitableFor.DRYNESS_MID
suitableFor.BARRIER_HIGH
```

이런 필드는 사용자 상태 의존적이므로 ProductProfile에 두지 않는다.  
사용자 상태에 따른 적합성 판단은 ProductSearchSpec과 RecommendationPolicy가 런타임에 수행한다.

## 9. Fallback

추천 후보가 부족해도 결과를 완전히 비우지 않는다.

```text
Hard Block은 유지
requiredTraits를 preferred로 완화
categoryHints를 완화
저위험 기본 관리 상품을 대안 추천으로 노출
```

사용자에게는 다음처럼 안내한다.

```text
현재 등록된 상품 중 조건을 정확히 만족하는 후보가 부족해요.
그래서 자극 가능성이 낮고 기본 관리에 가까운 대안 상품을 함께 보여드려요.
```

## 10. 결론

MVP 추천 시스템은 정밀 처방 엔진이 아니라,  
중첩 피부상태 해석 기반의 안전한 상품 후보 정렬 시스템이다.

핵심은 다음 순서다.

```text
피부상태 해석
-> 상품 검색 조건 생성
-> ProductProfile 후보 필터
-> 안전 규칙 적용
-> 랭킹과 설명 생성
```
