### ProductGroupScore
_Entity_

#### 속성 (property)
- `id` : 인조식별자 - ID
- `product` : 해당 점수가 어떤 Product에 속하는지
- `ingredientGroup` : 성분군
- `score` : 성분군 비율 점수
- `rank` : 성분군 순위

#### 행위
- `create()`: Product 내부에서만 호출되는 팩토리 메서드

#### 규칙
- ProductGroupScore는 Product에 종속된 엔티티이다
- 독립적으로 생성될 수 없다
- 생성은 Product의 addGroupScore()를 통해서만 이루어진다
- 외부에서 create()를 직접 호출하는 것은 금지된다
- (product, ingredientGroup)은 유일해야 한다
- score는 0 이상이어야 한다
- rank는 중복될 수 없다
- 생성 이후 score와 rank는 변경할 수 없다
- Product와의 연관관계는 생성 시점에 반드시 설정되어야 한다  