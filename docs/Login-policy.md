## 로그인 규칙 
- 로그인 요청 규칙 
  - 프론트는 로그인 완료 후 백엔드에 아래 정보를 보낸다 
    - `provider`
    - `authorizationCode`
    - 필요시 provider 전용 보조값
  - 예시 
    - Toss: `authorizationCode` , `referrer`
    - Google/Kakao 등 : authorizationCode 중심
  - 요청 DTO 대략
    - 공통 필드 
      - `provider`
      - `authorizationCode`
    - 선택 필드 
      - `referrer`
### TOSS 전용 규칙 
Toss handler 규칙
1. authorizationCode + referrer 로 Toss token교환 
2. Toss 사용자 조회 
3. userKey 추출 
4. providerUserId = userKey 로 공통 결과 반환 
> Toss 특수성은 Handler안에서 끝내야 함
---
## provider 분기 규칙 
백엔드는 `provider` 를 보고 로그인 전략을 선택 
- Toss -> TossLoginHandler
- Google -> GoogleLoginHandler
-> 공통흐름은 하나고 , 외부 인증 차이만 provider별로 처리 
---
## 외부 인증 결과 규칙 
각 provider handler 는 최종적으로 공통 사용자 식별 결과를 반환해야함
공통 결과에 필요한 최소 정보 
- provider
- providerUserId
- name
예
- Toss : providerUserId = userKey
- Google : providerUserId = sub
> 공통 서비스는 특정 provider를 몰라야함 
---
## 내부 회원 식별 규칙 
내부 회원은 다음과 같이 식별함 
- 내부 PK : UserID
- 외부 로그인 매핑 
  - UserOauth
    - provider
    - providerUserID
로그인 시 :
1. provider + providerUserId 로 UserOauth 조회 
2. 있으면 기존 회원 로그인 
3. 없으면 신규 회원 생성 
---
## 회원 생성 규칙 
신규 로그인 사용자는 자동 가입 처리 
생성 순서 : 
1. User생성 
2. UserOauth생성
3. 이름이 있으면 프로필 반영 
4. 이름이 없어도 가입은 가능해야함 
MVP 에서는 별도 회원가입 페이지 x 
소셜 로그인 = 가입 또는 로그인 
---
## JWT규칙
로그인 성공 시 우리 서비스 JWT를 발급
- 외부 provider 토큰은 서버 내부 처리용 
- 프론트에는 우리 JWT만 내려줌 
- MVP는 access token만 먼저 가도 됨 
- JWT claim 최소값은 userId
---
## Security 규칙 
- 로그인 API는 `permitAll`
- 나머지 API는 인증 필요 
- JWT필터는 우리 JWT만 검증 
- 필터는 provider 몰라도 됨 
---

