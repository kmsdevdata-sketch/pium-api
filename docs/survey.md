## 설문 문항 

현재 SkinMetric으로 지정된 항목은 
```text
    DRYNESS,            // 건조 상태
    BARRIER,            // 피부 장벽 상태
    OILINESS,           // 유분 피지 경향
    BLEMISH_PRONENESS,  // 트러블 발생 경향 / 피부 트러블 민감도
    SENSITIVITY,        // 민감성 / 자극 민감 반응
    PIGMENTATION_TONE,  // 색소침착 피부 톤 상태
    AGING_SIGNS         // 주름 탄력등 노화 징후
```
총 7가지 항목으로 선정된다 
각 항목에 대한 설문 문항 배분은 
```text
DRYNESS             : 2
OILINESS            : 2
BLEMISH             : 2
SENSITIVITY         : 2
PIGMENTATION        : 1
AGING               : 1
GOAL                : 1
```
로 문항 분배되며
- BARRIER 항목이 분배되지 않은 이유는 장벽과 관련된 질문은 사용자 인식이 어렵기 떄문에 DRYNESS / SENSITIVITY 에서 파생점수를 활용한다
- 색소/톤 , 주름 이 1개의 문항만 분배된 이유는 타 항목에 비하여 사용자가 인식하기 쉬운 영역이기 떄문이다 

GOAL : 사용자의 고민을 입력받는다 - 실제로는 건조 피부타입이라 하더라도 여드름 케어를 원할수 있기 떄문

추천의 정확도보다 더욱 중요한것은 안전성이다 
직결되는 항목은 **SENSITIVITY / BARRIER** 항목이며 단순히 여드름 수치가 높다고 하여 
강한제품을 추천하는것은 사용자의 피부타입을 고려하지 않고 피부가 뒤집히는 = 신뢰성을 낮추는 추천이다 

### 문제의식
위와 같이 설문형을 통해서 점수배분을 5가지로 배분을 하게되면 
점수차와 실제 상태차이의 차이가 발생하게 
> ex. DRYNESS = 10  BLEMISH = 70 => 실제로 7배차이??

그리하여 순서는 보장받지만 간격을 보장해주지는 않는다 
> IRT(Item Response Theory)

그리하여 IRT라는 방법론의 철학을 따온다 
핵심 아이디어는 : 모든 질문은 동일한 힘을 가지지 않는다고 가정한다 

각 설문의 질문에 대하여 모델링하는 방식이지만 MVP상태에서 도입은 과하다고 판단 철학만 도입
- 절대 점수보다 상대 상태로 해석 (`DRYNESS` = 72 => "상당히 건조한 경항이 있음")
- hard threshold (`BLEMISH` >= 70 => acne-care추천 x | BLEMISH 높으면 가중 증가)
- normalization 사용 (점수 벡털를 사용자 내부 상대비율로만 활용)

중요한점은 점수 배점을 확인하여 의학적 수치가 아닌 사용자의 피부 경향성의 강도를 파악 

## 실제 설문 문항 
```text
[섹션 A] 피부 상태 (8문항)
  Q1. DRYNESS ①    빈도형
  Q2. DRYNESS ②    행동 기준 상황 선택 (시간대 → 루틴 기준으로 수정)
  Q3. OILINESS ①   유분 상태
  Q4. OILINESS ②   유분 상황 복수 선택
  Q5. BLEMISH ①    트러블 빈도
  Q6. BLEMISH ②    트러블 부위
  Q7. SENSITIVITY ① 화장품 반응
  Q8. SENSITIVITY ② 환경 자극 반응

[섹션 B] 피부 고민 (2문항)
  Q9.  PIGMENTATION
  Q10. AGING

[섹션 C] 목표 (1문항)
  Q11. GOAL

총 11문항
```

총11개의 문항으로 구성하였다 
실제 설문 문항은 [설문_문항집](설문_문항집.md)참고

