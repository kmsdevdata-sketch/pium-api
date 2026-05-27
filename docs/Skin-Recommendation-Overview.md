# 스킨 추천 설계 개요 (MVP, 얇은 설계)

## 1. 목적
추천 시스템을 복잡한 점수 최적화가 아니라 "상태 해석 + 안전한 추천 방향 제시" 중심으로 운영한다.

## 2. 설계 원칙
- 추천 입력은 숫자 점수보다 상태 라벨을 우선 사용한다.
- 단일 지표 최적화가 아니라 중첩 상태 조합을 기준으로 판단한다.
- 안전성 신호(SENSITIVITY, BARRIER)는 효능 신호보다 우선한다.
- GOAL은 보조 신호로만 반영하고 안전 규칙을 뒤집지 않는다.
- 내부 점수는 7축 상태 그래프 시각화와 추천 연산에 사용한다.

## 3. 최소 추천 흐름
```text
설문 응답
-> 상태축 점수 계산(내부)
-> 상태 라벨 변환(LOW/MID/HIGH)
-> 조합 해석
-> SkinNeedProfile 생성
-> ProductProfile 후보 비교
-> 안전 상한선/위험 감점/목표 보정
-> 추천 결과 생성
```

## 4. 중간 해석 모델

추천 엔진은 `DRYNESS HIGH -> 보습 추천` 같은 단일 축 매핑만 사용하지 않는다.

`SkinAnalysisResult`를 바로 상품과 비교하지 않고,
추천이 소비할 수 있는 중간 언어인 `SkinNeedProfile`로 변환한다.

`SkinNeedProfile`은 다음 정보를 가진다.

- 상태축별 필요 trait 가중치
- 피해야 할 risk trait 가중치
- 현재 피부의 risk tolerance
- 주요 중첩 상태 패턴
- GOAL이 반영될 수 있는 범위
- 추천 설명에 사용할 care strategy

상세 내용은 [SkinNeedProfile](recommendation/SkinNeedProfile.md)을 기준으로 한다.

## 5. 안전 게이트와 안전 상한선
다음 조건이면 안전 우선 모드를 적용한다.

- SENSITIVITY = HIGH
- 또는 BARRIER = HIGH

단, 안전 우선 모드는 "장벽 제품만 추천한다"는 뜻이 아니다.

이 경우 추천은 다음 방식으로 제한한다.

- 명백히 위험한 후보는 제외한다.
- 강한 액티브/각질/자극 리스크 상품은 최대 추천 점수를 제한한다.
- 안전 기준을 통과한 상품 안에서는 다른 피부 고민과 GOAL도 반영한다.
- 트러블, 색소, 탄력 고민은 무시하지 않고 현재 피부가 감당 가능한 강도로 다룬다.

예:

```text
BARRIER HIGH + BLEMISH HIGH
-> 트러블 케어 필요
-> 하지만 강한 BHA/필링/고자극 액티브는 지연
-> 진정 + 장벽 + 약한 트러블 케어 상품을 우선
```

## 6. GOAL 반영 원칙
- GOAL은 상태를 덮어쓰지 않는다.
- GOAL은 동일 안전 조건 내에서만 추천 순서를 약하게 조정한다.
- 예: AGING 목표가 있어도 SAFE_ONLY 또는 CAUTION이면 공격적 기능성 추천을 지연한다.

## 7. 사용자 노출 원칙
추천 응답은 아래 항목만 우선 제공한다.

- 현재 상태 요약 1문장
- 축별 상태 라벨(LOW/MID/HIGH)
- 근거 문장(왜 이렇게 해석했는지)
- 추천 모드(SAFE_ONLY, CAUTION, BALANCED)
- 7축 상태 그래프(레이더/폴리곤) 시각화

숫자 점수는 노출하지 않는다.

## 8. 상품 추천 매핑 원칙
- 기존의 단순 필요 성분군 1:1 매핑에서 벗어나, 복합 상태 조합을 기준으로 추천한다.
- 추천 계산은 `상태 라벨 + 내부 점수 분포 + SkinNeedProfile + ProductProfile + 안전 상한선 + GOAL`을 함께 사용한다.
- 추천 결과마다 근거를 남긴다.
  - 어떤 상태 신호 조합이 작동했는지
  - 어떤 안전 규칙이 적용되었는지
  - 어떤 이유로 특정 상품군이 제외/지연되었는지
- 추천 설명은 "효능 주장"보다 "현재 상태에 맞는 루틴 방향" 중심으로 작성한다.

상품 원본 데이터는 추천 엔진이 직접 소비하지 않는다.

```text
ProductRawData
-> ProductProfiling / ACL
-> ProductProfile
-> RecommendationPolicy
```

상세 내용은 [Product Profiling](recommendation/ProductProfiling.md)과
[Recommendation Flow](recommendation/RecommendationFlow.md)를 기준으로 한다.

## 9. 결론
MVP 추천 시스템은 "정밀 점수 엔진"이 아니라 "중첩 상태 해석 기반의 안전한 상품 후보 정렬 시스템"이다.
초기에는 규칙을 얇게 유지하고, 사용자 피드백과 운영 데이터로 점진 개선한다.
