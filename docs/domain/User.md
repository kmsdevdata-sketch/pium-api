## 도메인 모델

### 사용자
- 사용자는 역할분리를 위해서 아래 3가지로 나뉜다
- User : 인증,권한의 주체
- UserOauth : 소셜 로그인 매핑 용도
- UserProfile : 사용자 프로필 관련 정보

### User
_Entity_
#### 속성(property)
- `id` : 인조식별자 - ID
- `role` : 권한 - USER/ADMIN
- `status` : 사용자의 상태 - ACTIVE/BANNED/DELETED
- `lastLoginAt` : 사용자의 마지막 로그인 시점
- `deletedAt` : 사용자 탈퇴 시점
#### 행위
- `create()` : 사용자 생성 (기본 상태 ACTIVE)
- `login()` : 로그인 시점 갱신
- `delete()` : 사용자 탈퇴 / 탈퇴 시점 갱신
- `ban()` : 사용자 차단
- `unban()` : 차단된 사용자 재활성화
#### 규칙
- DELETE 상태에서는 로그인 불가
- BANNED 상태에서는 로그인 불가
- 탈퇴시에 기존 User는 DELETE(탈퇴처리)하고 UserOauth는 유지한다
    - 재가입시에 기존 User가 아닌 새로운 User를 생성후 이전 UserOauth를 연결한다
#### 회원 상태(UserStatus)
_ENUM_
#### 상수
- `ACTIVE` : 활성 상태
- `DELETED` : 탈퇴 상태
- `BANNED` : 차단 상태

### UserOauth
_Entity_
#### 속성(property)
- `id` : 인조식별자 - ID
- `user` : OAuth계정이 어떠한 내부 User와 연결되어있는지
    - `User` 1 : N `UserOauth` 관계
- `provider` : 어떠한 소셜의 계정인지
    - ex.KAKAO,GOOGLE
- `providerUserId` : OAuth제공자가 발급하는 사용자 식별자
    - _provider_ 범위 내에서 유니크
    - 로그인시 User 조회 기준으로 사용
- `email` : OAuth제공자가 제공하는 사용자의 이메일
#### 행위
- `create()` : 소셜계정 생성
#### 규칙
- 하나의 UserOauth 는 하나의 User에게만 속해야한다
- `provider` : `providerUserId` 는 **unique** 해야 한다
- 동일 `providerUserID`로 중복 생성 불가

### UserProfile
_Entity_
#### 속성(property)
- `id` : 인조식별자 - ID
- `user` : 해당계정이 어떠한 User와 연결되어있는지
- `nickname` : 사용자가 설정한 어플리케이션 내 사용 닉네임
- `profileImage`: 사용자의 프로필 사진
#### 행위
- `changeNickname()` : 닉네임 변경
- `changeProfileImage()` : 프로필 사진 변경
#### 규칙
- `nickname`은 1글자 이상 10글자 이하여야 한다
- `profileImage`의 이미지 크기및 타입은 제한된다(추후 설정)

