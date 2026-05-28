# Skin Interpretation and Product Search Spec

## 1. Purpose

피움 추천은 `DRYNESS HIGH -> 보습 제품`처럼 단일 고민을 해결하는 방식이 아니다.

목표는 사용자의 7개 피부 상태와 최대 2개의 개선 목표를 함께 보고,  
"지금 이 피부가 감당 가능한 루틴/제품 조건"을 만든 뒤 상품을 필터링하고 정렬하는 것이다.

따라서 추천 전 단계는 두 개로 나눈다.

```text
SkinAnalysisResult + UserGoal
-> SkinInterpretation
-> ProductSearchSpec
```

`SkinInterpretation`은 피부 상태의 의미를 해석하고,  
`ProductSearchSpec`은 상품 후보를 조회/필터/랭킹하기 위한 조건이다.

## 2. Position

```text
SkinAnalysisResult
  - 7축 score/level
  - goals, 최대 2개

SkinInterpretation
  - 상태 조합 해석
  - goal 충돌 안내
  - routine intent 결정

ProductSearchSpec
  - required/preferred/blocked/caution trait
  - goal boost
  - category hint
  - fallback rule

ProductProfile
  - 상품 capability/risk/evidence 색인

RecommendationPolicy
  - hard gate
  - scoring
  - diversity
  - explanation
```

중요한 경계:

- ProductProfile에 `suitableFor.DRYNESS_MID` 같은 사용자 상태 의존 필드를 저장하지 않는다.
- ProductProfile은 상품의 `benefit/risk/evidence`만 표현한다.
- ProductSearchSpec이 ProductProfile을 소비한다.

## 3. SkinInterpretation v1

MVP에서는 얇게 시작한다.

```json
{
  "resultId": "sar_123",
  "interpretationVersion": "skin-interpretation-v1",
  "summaryType": "LIGHT_BALANCE",
  "routineIntent": "LIGHT_HYDRATION_BALANCE",
  "primaryNeeds": [
    { "trait": "HYDRATION_SUPPORT", "intensity": "PREFERRED", "source": "DRYNESS_MID" }
  ],
  "secondaryNeeds": [
    { "trait": "BARRIER_SUPPORT", "intensity": "PREFERRED", "source": "BARRIER_MID" }
  ],
  "riskConstraints": [
    { "trait": "HEAVY_OCCLUSIVE_RISK", "policy": "CAUTION", "source": "OILINESS_MID" }
  ],
  "goalNeeds": [
    { "goal": "Q11_1", "trait": "HYDRATION_SUPPORT", "boost": "LOW" }
  ],
  "goalConflictNotices": []
}
```

필드 의미:

| 필드 | 의미 |
| --- | --- |
| summaryType | 상태 조합의 요약 타입 |
| routineIntent | 추천 방향. 예: 장벽 우선, 가벼운 균형, 저자극 톤 케어 |
| primaryNeeds | 현재 피부 상태상 우선 필요한 trait |
| secondaryNeeds | 있으면 좋은 보조 trait |
| riskConstraints | 상품 후보가 피하거나 주의해야 할 risk |
| goalNeeds | 사용자가 선택한 개선 목표가 만든 boost |
| goalConflictNotices | goal과 현재 피부상태가 충돌할 때 사용자 안내에 쓰는 구조 |

## 4. ProductSearchSpec v1

`SkinInterpretation`은 최종적으로 ProductSearchSpec을 만든다.

```json
{
  "requiredTraits": [
    { "trait": "BARRIER_SUPPORT", "minStrength": "WEAK" }
  ],
  "preferredTraits": [
    { "trait": "HYDRATION_SUPPORT", "weight": "MEDIUM" },
    { "trait": "SOOTHING_SUPPORT", "weight": "MEDIUM" }
  ],
  "goalBoostTraits": [
    { "trait": "BLEMISH_CARE_SUPPORT", "weight": "LOW", "goal": "Q11_2" }
  ],
  "blockedRiskTraits": [
    "STRONG_EXFOLIATION_EFFECT",
    "HIGH_IRRITATION_RISK"
  ],
  "cautionRiskTraits": [
    "FRAGRANCE_OR_ALLERGEN_RISK",
    "DRYING_OR_STRIPPING_RISK"
  ],
  "categoryHints": [
    "TONER",
    "ESSENCE_SERUM",
    "LOTION_CREAM"
  ],
  "fallbackPolicy": "RELAX_PREFERRED_KEEP_BLOCKED"
}
```

ProductSearchSpec은 DB 검색 조건이면서 scoring 입력이다.

처리 순서:

```text
1. blockedRiskTraits로 hard gate
2. requiredTraits가 있으면 우선 충족 후보 확보
3. preferredTraits와 goalBoostTraits로 점수화
4. cautionRiskTraits는 감점과 안내 문구 생성
5. categoryHints는 다양성/우선 노출 보정
6. 후보 부족 시 fallbackPolicy 적용
```

## 5. Level Handling

`HIGH`만 추천을 움직이면 LOW/MID가 많은 실제 유저에게 추천 기준이 약해진다.

MVP 원칙:

| Level | 추천 의미 |
| --- | --- |
| HIGH | REQUIRED 또는 BLOCKED/CAPPED 강하게 작동 |
| MID | PREFERRED 또는 CAUTION으로 부드럽게 작동 |
| LOW | 기본 무가점. 단, 과잉 케어 방지나 조합 보정에 사용 |

예:

| 상태 | 해석 |
| --- | --- |
| DRYNESS MID | HYDRATION_SUPPORT를 preferred로 둔다 |
| BARRIER MID | BARRIER_SUPPORT를 preferred로 두고 강한 자극은 caution |
| OILINESS MID | SEBUM_CONTROL_SUPPORT를 preferred로 두되 heavy occlusive는 caution |
| SENSITIVITY LOW | 별도 hard gate를 만들지 않는다 |
| BLEMISH LOW | 트러블 goal이 없으면 blemish trait를 강하게 요구하지 않는다 |

## 6. Goal Handling

현재 설문 goal은 최대 2개다.

| Code | Goal | 연결 trait |
| --- | --- | --- |
| Q11_1 | 보습·수분감 | HYDRATION_SUPPORT, BARRIER_SUPPORT |
| Q11_2 | 트러블·여드름 | BLEMISH_CARE_SUPPORT, SEBUM_CONTROL_SUPPORT |
| Q11_3 | 피부 톤·미백 | BRIGHTENING_SUPPORT, UV_PROTECTION |
| Q11_4 | 모공·피지 | SEBUM_CONTROL_SUPPORT, EXFOLIATION_EFFECT |
| Q11_5 | 탄력·주름 | ANTI_AGING_SUPPORT, UV_PROTECTION |
| Q11_6 | 민감한 피부 진정·장벽 강화 | SOOTHING_SUPPORT, BARRIER_SUPPORT |

원칙:

- goal은 피부 상태를 override하지 않는다.
- goal은 안전 조건 안에서 priority boost로 작동한다.
- goal과 피부상태가 충돌하면 추천 모드를 나누지 않고 안내 문구를 제공한다.

예:

```text
BARRIER HIGH + goal Q11_2
-> 트러블 케어 goal은 반영
-> 강한 BHA/필링/고자극 액티브는 blocked 또는 caution
-> 장벽을 해치지 않는 mild blemish care 우선
```

사용자 안내 예:

```text
선택하신 목표는 트러블 케어지만, 현재는 장벽 부담 신호가 함께 보여요.
피움은 자극 가능성이 큰 제품보다 장벽을 해치지 않는 트러블 케어 제품을 우선 추천해요.
```

## 7. Composite Rules

모든 조합을 if문으로 만들지 않는다.

기본은 각 축의 level이 ProductSearchSpec을 만들고,  
도메인상 안전 판단이 중요한 조합만 composite rule로 보정한다.

| 조합 | 해석 | ProductSearchSpec 영향 |
| --- | --- | --- |
| BARRIER HIGH + BLEMISH MID/HIGH | 트러블 관리는 필요하지만 장벽 허용도가 낮음 | BARRIER/SOOTHING 우선, strong exfoliation/high irritation block |
| SENSITIVITY HIGH + BLEMISH MID/HIGH | 트러블 신호와 자극 반응이 함께 있음 | mild blemish care만 boost, fragrance/high active caution |
| DRYNESS MID/HIGH + OILINESS MID/HIGH | 수분 부족과 피지 신호가 공존 | hydration preferred, heavy occlusive caution, drying cleanser caution |
| PIGMENTATION MID/HIGH + SENSITIVITY MID/HIGH | 톤 케어 필요하지만 자극 허용도 낮음 | UV/low-irritation brightening 우선, strong acid/vitamin C caution |
| AGING MID/HIGH + BARRIER MID/HIGH | 탄력 고민은 있으나 장벽 안정 선행 필요 | UV/barrier/low-irritation anti-aging 우선, retinoid-like strong active caution |
| BARRIER MID/HIGH + goal Q11_2/Q11_4 | 트러블/피지 goal이 있어도 공격적 각질 케어를 제한 | goal boost는 낮추고 safety constraint 유지 |

## 8. Safety Policy Levels

상세 safety gate는 [Safety Policy](SafetyPolicy.md)를 기준으로 한다.

| 정책 | 의미 |
| --- | --- |
| Hard Block | 현재 상태에서 추천 후보 제외 |
| Soft Penalty | 후보에는 남기되 점수 크게 감점 |
| Caution | 점수 소폭 감점 또는 유지, 대신 주의 문구 생성 |

예:

```text
BARRIER HIGH + STRONG_EXFOLIATION_EFFECT
-> Hard Block 또는 Soft Penalty

SENSITIVITY MID + FRAGRANCE_OR_ALLERGEN_RISK
-> Caution

OILINESS MID + HEAVY_OCCLUSIVE_RISK
-> Caution 또는 Soft Penalty
```

## 9. Fallback Policy

추천 후보가 부족할 때도 결과를 완전히 비우지 않는다.

우선순위:

```text
1. Hard Block은 유지
2. requiredTraits를 preferred로 완화
3. categoryHints를 완화
4. 기본 보습/진정/선케어에 가까운 저위험 상품을 대안 추천으로 노출
```

사용자 안내 예:

```text
현재 등록된 상품 중 조건을 정확히 만족하는 후보가 부족해요.
그래서 자극 가능성이 낮고 기본 관리에 가까운 대안 상품을 함께 보여드려요.
```

## 10. Domain Evidence Notes

MVP safety rule은 임의 감이 아니라 다음 도메인 지식을 기준으로 잡는다.

- 여드름성 피부도 건조해질 수 있고 보습이 필요할 수 있다. 강한 acne 성분이나 exfoliant는 건조/자극을 키울 수 있다.
- salicylic acid 등 각질/트러블 관련 성분은 acne에 도움이 될 수 있지만, peeling/discomfort 또는 irritation risk가 있다.
- ceramide, cholesterol, free fatty acid는 stratum corneum barrier와 연결되는 핵심 lipid group이다.
- fragrance는 cosmetic allergic contact dermatitis의 중요한 원인군이므로 SENSITIVITY/BARRIER 상태에서 risk trait로 다룬다.

참고:

- AAD, acne skin care and moisturizer: https://www.aad.org/public/diseases/acne/skin-care/moisturizer
- AAD, acne treatment side-effect skincare cautions: https://www.aad.org/public/diseases/acne/derm-treat/isotretinoin/side-effects
- Skin Barrier Dysfunction in Acne Vulgaris: https://pmc.ncbi.nlm.nih.gov/articles/PMC11650898/
- Ceramides and skin function: https://pubmed.ncbi.nlm.nih.gov/12553851/
- Topical salicylic acid and other acne ingredients review: https://pmc.ncbi.nlm.nih.gov/articles/PMC7193765/
- Fragrance contact allergy review: https://pmc.ncbi.nlm.nih.gov/articles/PMC11334351/
