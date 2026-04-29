### 설문결과(SkinResult)
- SkinResult는 Immutable Aggregate 로써 설문결과에 접근하기 위한 유일한 진입점의 역할이다
- User 1 : N SkinResult 의 관계이다

### SkinResult
_Entity_
#### 속성(property)
- `id` : 인조식별자 - ID
- `user` : 해당 설문결과가 어떠한 User의 설문결과인지
- `top1Group` : 사용자의 필요 성분군 1위
- `top2Group` : 사용자의 필요 성분군 2위
- `diagnosedAt` : 설문 검사 시간
#### 행위
- `create()` : 설문 결과 기반 SkinResult생성 
- `addGroupScore()` : 설문결과 성분군 점수를 저장한다
#### 규칙
- 설문결과를 기반으로 생성된다 
- 변경 불가능한 결과 스냅샷이다 
- `top1Group`,`top2Group`은 SkinResult 생성시점에 SkinResultGroupScore를 기반으로 생성된 결과 스냅샷이며 불변이다 
  - SkinUxProfile을 획득하기 위한 Key역할을 수행한다 
