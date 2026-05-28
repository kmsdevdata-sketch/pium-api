# Recommendation Safety Policy

## 1. Purpose

이 문서는 피움 추천에서 safety gate가 어떻게 작동하는지 정의한다.

피움의 안전 정책은 "위험한 성분을 무조건 나쁜 상품으로 본다"가 아니다.  
같은 상품도 사용자의 `BARRIER`, `SENSITIVITY`, `DRYNESS`, `BLEMISH_PRONENESS`, `OILINESS` 조합에 따라 추천 강도가 달라진다.

핵심 원칙:

```text
기능이 강한 성분일수록,
BARRIER/SENSITIVITY/DRYNESS 신호가 높을 때 gate 강도를 올린다.
```

## 2. Policy Levels

| Level | 의미 | 추천 처리 |
| --- | --- | --- |
| Hard Block | 현재 상태에서 추천 후보로 부적절 | 후보 제외 |
| Soft Penalty | 추천은 가능하지만 상위 노출 제한 | 큰 감점, 이유 저장 |
| Caution | 추천 가능하지만 주의 필요 | 소폭 감점 또는 안내 문구 |

MVP에서는 완벽한 정량 판단보다 관측 가능한 신호 기반의 보수적 정책을 우선한다.

## 3. Observable Signals

상품 원본에서 바로 알 수 있는 것과 알 수 없는 것을 구분한다.

| 판단 항목 | 직접 판단 가능 여부 | MVP 처리 |
| --- | --- | --- |
| 성분 존재 | 가능 | ingredientText에서 정규화 |
| 성분 순서 | 일부 가능 | 상위/중위/하위 위치로 strength 추정 |
| 정확한 함량 | 대부분 불가 | 공개 claim이 있을 때만 사용 |
| pH | 대부분 불가 | 직접 claim 없으면 사용하지 않음 |
| 고농도/저농도 | 대부분 불가 | 함량 claim 또는 성분 위치/카테고리/문구 proxy 사용 |
| 실제 자극성 | 불가 | risk trait와 confidence로만 표현 |

예:

```text
"BHA 2% 초과"는 전성분만으로 알 수 없다.
따라서 MVP에서는 함량 claim이 없으면
EXFOLIATOR 카테고리 + salicylic acid 존재 + 필링/각질 claim 조합을 STRONG_EXFOLIATION_EFFECT proxy로 본다.
```

## 4. Trait Mapping

현재 문서 trait와 코드 enum은 아직 완전히 1:1로 구현되어 있지 않다.

| 글의 표현 | 피움 trait/policy 명칭 | 비고 |
| --- | --- | --- |
| 강한 액티브 | STRONG_ACTIVE_RISK | retinoid-like, high-strength vitamin C, strong acid 등 |
| 강한 필링/산 | STRONG_EXFOLIATION_EFFECT | AHA/BHA 복합, EXFOLIATOR 카테고리, 강한 필링 claim |
| 약한 BHA | EXFOLIATION_EFFECT + caution | 함량 미공개면 약/강 단정 금지 |
| 고농도 비타민C | STRONG_ACTIVE_RISK | L-ascorbic acid + 20% 이상 claim 있을 때만 확정 |
| 안정형 유도체 | BRIGHTENING_SUPPORT | ascorbyl glucoside, ethyl ascorbic acid 등 |
| 중질 오클루시브 | HEAVY_OCCLUSIVE_RISK | 오일/밤/리치 크림, heavy oil/wax 성분 |
| 코메도제닉 오일 | COMEDOGENIC_RISK | 개인차가 커서 hard block 남발 금지 |
| 장벽 회복 우선 | BARRIER_SUPPORT + SOOTHING_SUPPORT | ProductSearchSpec required/preferred로 반영 |

## 5. Common Gate Logic

개별 케이스를 100개 if문으로 만들지 않는다.  
먼저 공통 gate를 적용하고, 도메인상 중요한 조합만 보정한다.

```text
if product has STRONG_ACTIVE_RISK or STRONG_EXFOLIATION_EFFECT:
  if BARRIER == HIGH or SENSITIVITY == HIGH:
    gate = stronger

if product has DRYING_OR_STRIPPING_RISK:
  if DRYNESS == HIGH or BARRIER == HIGH:
    gate = stronger

if product has HEAVY_OCCLUSIVE_RISK or COMEDOGENIC_RISK:
  if OILINESS == HIGH or BLEMISH_PRONENESS == HIGH:
    gate = stronger
```

## 6. Safety Matrix v1

| 조합 | Hard Block | Soft Penalty | 우선 추천 방향 | Caution 문구 |
| --- | --- | --- | --- | --- |
| BARRIER HIGH + BLEMISH MID/HIGH | STRONG_EXFOLIATION_EFFECT, HIGH_IRRITATION_RISK | EXFOLIATION_EFFECT, STRONG_ACTIVE_RISK | BARRIER_SUPPORT + SOOTHING_SUPPORT + mild BLEMISH_CARE_SUPPORT | 장벽 부담이 있을 때 강한 각질/트러블 케어는 자극을 키울 수 있어요. |
| BARRIER HIGH + AGING_SIGNS MID/HIGH | strong retinoid-like active proxy | retinoid-like present, strong anti-aging claim | BARRIER_SUPPORT + SOOTHING_SUPPORT, peptide/adenosine 계열 | 장벽 안정 후 기능성 성분을 단계적으로 도입하는 편이 좋아요. |
| SENSITIVITY HIGH + PIGMENTATION_TONE MID/HIGH | high-strength L-ascorbic acid claim, strong acid brightening | L-ascorbic acid present, strong brightening/peeling claim | UV_PROTECTION + low-irritation BRIGHTENING_SUPPORT | 민감 반응이 잦다면 저자극 톤 케어부터 시작하는 편이 좋아요. |
| OILINESS HIGH + DRYNESS MID/HIGH | heavy occlusive + high comedogenic proxy | HEAVY_OCCLUSIVE_RISK | HUMECTANT 중심 HYDRATION_SUPPORT + lightweight BARRIER_SUPPORT | 수분은 필요하지만 무거운 오일막은 답답함이나 트러블 부담이 될 수 있어요. |
| BLEMISH_PRONENESS HIGH + DRYNESS MID/HIGH | STRONG_EXFOLIATION_EFFECT, DRYING_OR_STRIPPING_RISK | EXFOLIATION_EFFECT, acne active with drying risk | niacinamide/panthenol 등 진정+트러블 보조 | 건조한 상태에서 강한 각질 케어는 장벽 부담을 키울 수 있어요. |
| SENSITIVITY HIGH + EXFOLIATION_EFFECT | AHA+BHA complex proxy, high glycolic/peeling claim | low-strength AHA/BHA proxy | PHA 또는 soothing 동반 제품 | 민감 반응이 있다면 낮은 빈도와 패치 테스트가 필요해요. |
| AGING_SIGNS MID/HIGH + SENSITIVITY HIGH | strong retinoid-like active proxy, strong peeling | retinoid-like present | peptide, adenosine, niacinamide, UV_PROTECTION | 탄력 케어는 가능하지만 자극 임계값을 낮게 보고 순한 경로를 우선해요. |

## 7. Case Notes

### 7.1 BARRIER HIGH + AGING_SIGNS HIGH

노화 케어 goal 또는 `AGING_SIGNS`가 높아도 장벽 부담이 높으면 strong active를 먼저 밀지 않는다.

MVP proxy:

- `STRONG_ACTIVE_RISK`: retinol/retinal/tretinoin-like 표현, 고강도 anti-aging claim, retinoid 계열 성분
- `Hard Block`: retinoid-like + 강한 claim + BARRIER HIGH/SENSITIVITY HIGH
- `Soft Penalty`: retinoid-like 성분 존재만 확인되는 경우

우선 추천:

- BARRIER_SUPPORT
- SOOTHING_SUPPORT
- ANTI_AGING_SUPPORT 중 peptide/adenosine/niacinamide 기반

### 7.2 SENSITIVITY HIGH + PIGMENTATION_TONE HIGH

톤 케어는 유지하되, 강한 산성 L-ascorbic acid나 필링 기반 브라이트닝은 제한한다.

MVP proxy:

- `Hard Block`: L-ascorbic acid 20% 이상처럼 함량 claim이 명시된 경우
- `Soft Penalty`: L-ascorbic acid 존재 + strong brightening claim
- `Preferred`: vitamin C derivative, niacinamide, UV_PROTECTION

주의:

- 함량이 없으면 고농도라고 단정하지 않는다.
- pH 정보가 없으면 low-pH 여부를 추론하지 않는다.

### 7.3 OILINESS HIGH + DRYNESS HIGH

보습이 필요하지만 무거운 occlusive만 밀면 답답함/트러블 부담이 커질 수 있는 조합이다.

MVP proxy:

- `HEAVY_OCCLUSIVE_RISK`: OIL_BALM 카테고리, rich balm/cream claim, wax/heavy oil group
- `COMEDOGENIC_RISK`: coconut oil, lanolin 등 known concern 성분

주의:

- mineral oil, petrolatum 같은 성분은 사람마다 반응이 다르고, 무조건 comedogenic으로 단정하지 않는다.
- MVP에서는 `OILINESS HIGH + BLEMISH HIGH`가 동반될 때 gate를 더 강하게 적용한다.

### 7.4 BLEMISH_PRONENESS HIGH + DRYNESS HIGH

트러블 케어는 필요하지만 건조 상태에서는 강한 각질/탈지 제품이 장벽 부담을 키울 수 있다.

MVP proxy:

- `Hard Block`: EXFOLIATOR 카테고리 + AHA/BHA 복합 + strong peeling claim
- `Soft Penalty`: salicylic acid 존재만 확인되는 leave-on 제품
- `Preferred`: niacinamide, panthenol, soothing group을 동반한 mild blemish care

### 7.5 SENSITIVITY HIGH + EXFOLIATION_EFFECT

민감 신호가 높으면 각질 케어는 빈도/강도 안내가 필요하다.

MVP proxy:

- `Hard Block`: AHA+BHA 복합, glycolic acid high-strength claim, peeling solution/pad 강한 claim
- `Soft Penalty`: 단일 AHA/BHA 존재
- `Preferred`: PHA, soothing group 동반 exfoliation

### 7.6 AGING_SIGNS HIGH + SENSITIVITY HIGH

노화 케어 욕구는 반영하되 자극 임계값을 낮게 본다.

MVP proxy:

- `Hard Block`: strong retinoid-like + strong peeling
- `Soft Penalty`: retinoid-like present
- `Preferred`: peptide, adenosine, niacinamide, UV_PROTECTION

## 8. Evidence Notes

- AAD는 retinoid 제품을 건조/알레르기 피부에 신중히 보고, 낮은 강도와 천천히 도입하는 방식을 권한다.
- AAD는 acne 피부도 건조할 수 있고 보습제가 도움이 될 수 있다고 안내한다.
- Vitamin C 리뷰에서는 20% 초과 농도가 생물학적 이득을 더 늘리지 않으며 자극 가능성을 높일 수 있다고 설명한다.
- Salicylic acid 등 acne 관련 성분은 도움 가능성이 있지만 peeling/discomfort 또는 irritation risk가 있다.
- Ceramide, cholesterol, fatty acid는 stratum corneum barrier lipid와 연결된다.
- Fragrance는 cosmetic allergic contact dermatitis의 중요한 원인군이다.

참고:

- AAD, retinoid or retinol: https://www.aad.org/public/everyday-care/skin-care-secrets/anti-aging/retinoid-retinol
- AAD, acne skin care and moisturizer: https://www.aad.org/public/diseases/acne/skin-care/moisturizer
- AAD, acne treatment side-effect skincare cautions: https://www.aad.org/public/diseases/acne/derm-treat/isotretinoin/side-effects
- Topical Vitamin C review: https://pmc.ncbi.nlm.nih.gov/articles/PMC5605218/
- Vitamin C in dermatology: https://pmc.ncbi.nlm.nih.gov/articles/PMC3673383/
- Topical salicylic acid and other acne ingredients review: https://pmc.ncbi.nlm.nih.gov/articles/PMC7193765/
- Skin Barrier Dysfunction in Acne Vulgaris: https://pmc.ncbi.nlm.nih.gov/articles/PMC11650898/
- Ceramides and skin function: https://pubmed.ncbi.nlm.nih.gov/12553851/
- Fragrance contact allergy review: https://pmc.ncbi.nlm.nih.gov/articles/PMC11334351/
