### Routine
_Entity_

#### 속성 (property)
- `id` : 인조식별자 - ID
- `routineGroup` : 해당 루틴이 어떤 RoutineGroup에 속하는지
- `routineType` : 루틴 타입 (AM / PM)

#### 행위
- `create()` : RoutineGroup 생성 과정에서 Routine을 생성한다 (내부용 팩토리)

#### 규칙
- Routine은 RoutineGroup에 종속된 엔티티이다
- Routine은 독립적으로 생성될 수 없다
- Routine은 AM 또는 PM 중 하나의 타입을 가진다
- 하나의 RoutineGroup은 AM, PM 각각 1개의 Routine을 가진다
- Routine은 시간대 구분을 위한 구조적 엔티티이며 별도의 도메인 의미를 가지지 않는다
- 생성 이후 변경되지 않는 불변 스냅샷이다

#### routineType
- AM , PM : 해당 루틴의 시간대 설정 