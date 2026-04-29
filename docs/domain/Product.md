### Product
_Entity_

#### 속성 (property)
- `id` : 인조식별자 - ID
- `name` : 상품명
- `brand` : 브랜드명
- `category` : 상품 카테고리
- `productUsageTime` : 사용 시간대 (AM / PM)
- `price` : 가격
- `description` : 상품 설명
- `imageUrl` : 상품 이미지 URL
- `purchaseUrl` : 구매 링크
- `active` : 노출 여부

#### 행위
- `create()` : 상품을 생성한다
- `activate()` : 상품을 활성화한다
- `deactivate()` : 상품을 비활성화한다

- `addGroupScore()` : 성분군 점수를 생성하여 추가한다  
  - 내부적으로 ProductGroupScore를 생성한다  
  - 동일 IngredientGroup 중복 추가 불가  
  - score / rank 유효성 검증 수행


#### 규칙
- Product는 Aggregate Root이다
- active가 false인 상품은 추천 대상에서 제외된다
- (product, ingredientGroup)은 유일해야 한다
- 하나의 Product는 각 IngredientGroup에 대해 최대 1개의 Score만 가진다
- ProductGroupScore는 Product의 상태와 함께 관리된다  