### 하루 루틴 세트(RoutineGroup)


_Entity_

#### 속성 (property)
- `id` : 인조식별자 - ID
- `user` : 해당 루틴이 어떤 사용자에게 속하는지
- `skinResult` : 해당 루틴이 어떤 피부 진단 결과를 기반으로 생성되었는지
- `title` : 루틴 식별을 위한 제목
#### 행위
- `create()` : SkinResult를 기반으로 루틴 그룹을 생성한다
- `addRoutine()` : Routine을 추가한다
    - AM, PM 두개 Routine생성
#### 규칙
- RoutineGroup은 루틴 생성의 기준 단위이며 Aggregate Root이다
  - Routine은 RoutineGroup 없이 존재할 수 없다
  - RoutineProduct는 Routine 없이 존재할 수 없다
- RoutineGroup은 SkinResult 기반으로 생성되며, 생성 이후 변경되지 않는 불변 스냅샷이다
- 하나의 RoutineGroup은 AM / PM 두 개의 Routine을 가진다
- RoutineGroup은 추천 결과의 단위이며, 생성 이후 내부 구성(Routine, Product)은 변경할 수 없다
- skinResult는 루틴 생성의 근거 데이터로 반드시 존재해야 한다