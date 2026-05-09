# 추천 도메인 설계 정리 (근거 기반)

## 1. 추천 방식과 알고리즘 설명

추천은 2단계 계산으로 분리한다.

1. 피부 해석 단계 (`SkinAnalysis`)
- 입력: 설문/사용자 상태 입력
- 처리: 피부 상태군 점수화 (`SkinMetricScore`, 0~100)
- 출력: 필요 성분군 가중치 (`RequiredIngredient`, 0~100)

2. 상품 매칭 단계 (`Recommendation`)
- 입력: 필요 성분군 + 상품 데이터
- 처리: 성분 적합도 계산 -> 안전 보정 -> 정규화 -> 랭킹
- 출력: 추천 상품 목록

핵심 설계 원칙
- 해석과 추천 실행을 분리한다.
- 상태군과 성분군은 다대다 가중치로 연결한다.
- 안전 보정(민감/장벽)을 효능보다 우선 적용한다.

---

## 2. 피부상태군과 선별 이유 (근거 포함)

MVP 상태군(8개):
- `DRYNESS`
- `SEBUM`
- `ACNE`
- `SENSITIVITY`
- `REDNESS`
- `PIGMENTATION`
- `BARRIER_WEAKNESS`
- `PHOTOAGING`

선별 이유:
- `DRYNESS`, `BARRIER_WEAKNESS`: 보습/장벽 우선순위 결정에 필요
    - 근거: 장벽 손상 피부에서 보습·장벽 보호의 중요성
    - 출처: National Eczema Association moisturization guidance
- `SENSITIVITY`, `REDNESS`: 자극 리스크 통제와 저자극 전략에 필요
    - 근거: 민감/홍조 피부에서 gentle, fragrance 최소화, 자외선 관리 권고
    - 출처: AAD rosacea/pregnancy skin care guidance
- `ACNE`, `SEBUM`: 여드름/피지 관련 성분 및 강도 결정에 필요
    - 근거: 여드름 관리 성분 사용 시 효능-자극 균형 필요
    - 출처: AAD acne guidance
- `PIGMENTATION`, `PHOTOAGING`: 자외선 차단/톤/노화 관리 우선순위에 필요
    - 근거: 색소·광노화 관리에서 광보호가 핵심
    - 출처: AAD melasma, FDA sunscreen guidance

---

## 3. 성분군과 선별 이유 (근거 포함)

MVP 성분군(10개):
- `HYDRATION`
- `BARRIER_REPAIR`
- `SEBUM_CONTROL`
- `ACNE_CARE`
- `SOOTHING`
- `BRIGHTENING`
- `TURNOVER`
- `ANTI_AGING`
- `PHOTOPROTECTION`
- `GENTLE_CLEANSING`

선별 이유:
- `PHOTOPROTECTION`: 색소/홍조/광노화 악화 방지의 기반축
    - 근거: broad-spectrum, SPF 기준, 재도포 권고
    - 출처: FDA, AAD
- `HYDRATION`, `BARRIER_REPAIR`, `GENTLE_CLEANSING`: 민감/장벽저하 사용자 안전 확보
    - 근거: 보습/장벽 보호/순한 세정 권고
    - 출처: AAD, National Eczema Association
- `ACNE_CARE`, `SEBUM_CONTROL`, `TURNOVER`: 여드름/면포/피지 문제 대응
    - 근거: 여드름 관련 유효 성분군 존재, 단 안전조건 필요
    - 출처: AAD acne guidance
- `SOOTHING`: 자극/홍조 완화 축
- `BRIGHTENING`: 색소 문제 대응 축
- `ANTI_AGING`: 광노화/탄력 저하 대응 축

운영 원칙:
- 특정 성분군 점수가 높아도 민감/장벽 점수가 높으면 강도 제한 보정한다.
- “필요 없음”이 아니라 “고강도 제한”으로 해석한다.

---

## 4. Conditional Adoption (After Product Domain Expansion)

아래는 Product 도메인 확장 후 적용한다.

필요 메타데이터:
- `productCategory` (cleanser/serum/cream/sunscreen 등)
- `leaveOnType` (rinse-off/leave-on)
- `usageTime` (AM/PM/BOTH)
- `activeStrength` (LOW/MEDIUM/HIGH/UNKNOWN)
- `fragranceFlag`
- `pregnancyCautionFlag`
- `comedogenicRisk`

적용 원칙:
- Product 도메인에서 제공되지 않는 메타에 의존한 규칙은 차용하지 않는다.
- 현재 단계는 성분군 기반 추천 + 안전 보정까지만 적용한다.

---

## 5. ReasonCode 재정리

결론:
- 고정 enum을 대량 정의해 초기부터 강제하지 않는다.

대신:
1. 추천 결과에 설명 슬롯을 둔다 (`primaryReasons`, `safetyAdjustments`)
2. 운영 로그를 통해 반복 패턴을 수집한다
3. 상위 빈출 설명만 표준 코드로 수렴한다

이유:
- 초기엔 케이스 다양성이 높아 고정 코드셋이 쉽게 과소/과대적합된다.
- 먼저 품질/설명 로그를 축적한 뒤 표준화하는 편이 안전하다.

---

## 참고 출처

- FDA sun safety / sunscreen labeling guidance
    - https://www.fda.gov/consumers/consumer-updates/tips-stay-safe-sun-sunscreen-sunglasses
- AAD melasma self-care
    - https://www.aad.org/dermatology-a-to-z/diseases-and-treatments/m---p/melasma/tips
- AAD rosacea trigger prevention
    - https://www.aad.org/rosacea-prevent-triggers
- AAD pregnancy skin care / ingredient caution
    - https://www.aad.org/public/everyday-care/skin-care-secrets/routine/pregnancy-skin-care
- AAD acne treatment in pregnancy
    - https://www.aad.org/public/diseases/acne/derm-treat/pregnancy
- National Eczema Association moisturizing guidance
    - https://nationaleczema.org/eczema/treatment/moisturizing/
