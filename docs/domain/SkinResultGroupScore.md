### 설문결과 성분군 점수(SkinResultGroupScore)
- SkinResulGroupScore는 사용자가 행한 설문결과의 각 추천 성분군 데이터를 담고있다
- SkinResult 1 : N SkinResultGroupScore 의 관계이다

### SkinResultGroupScore
_Entity_
#### 속성(property)
- `id` : 인조식별자 - ID
- `skinResult` : 해당 성분군 데이터가 어떠한 SkinResult의 설문결과인지
- `ingredientGroup` : 해당 성분군 데이터가 어떠한 성분군에 해당 하는지 
- `score` : 해당 성분군 데이터의 점수
- `priority` : 해당 성분군 데이터의 순위(rank는 DB오류로 인하여 priority로 표기)
#### 행위
- `create()` : 설문 결과 기반 SkinResultGroupScore 생성
  - SkinResult생성 과정에서 호출 
#### 규칙
- SkinResultGroupScore 는 SkinResult에게 종속된 엔티티이다 
- 독립적으로 생성될 수 없다
- 하나의 SkinResult는 각 IngredientGroup에 대하여 1개의 Score만 가진다 
- priority는 score기반 정렬 결과이며 중복될수 없다 
- SkinResult 생성시점에 설문 결과를 기반으로 사용자의 각 성분군 점수를 저장한다  
- 생성이후 불변이다
#### IngredientGroup
_ENUM_ 상수
- ACNE,           // 여드름 케어
- SEBUM_CONTROL,  // 피지 조절
- SOOTHING,       // 진정
- HYDRATION,      // 수분 공급
- BARRIER,        // 피부 장벽 강화
- BRIGHTENING,    // 미백 / 톤 개선
- TURNOVER,       // 각질 제거 / 재생
- ANTI_AGING      // 주름 개선 / 탄력 강화

