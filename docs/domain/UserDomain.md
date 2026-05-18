# User Domain

## 1. Overview
User 도메인은 서비스의 인증된 사용자를 관리한다.  
OAuth 기반 인증을 통해 사용자를 식별하며, 시스템 내 최소한의 식별 정보만을 책임진다.

이 도메인은 인증과 식별에만 집중하며, 피부 상태나 추천과 같은 비즈니스 도메인 정보는 포함하지 않는다.
---
## 2. Responsibility

- OAuth 기반 사용자 식별
- 시스템 내부 사용자 생성 및 관리
- 사용자 고유 식별자 제공
---
## 3. Domain Model

### 3.1 User
서비스의 인증된 사용자

필드:
- id
- oauthProvider (e.g. GOOGLE, KAKAO)
- oauthId (provider에서 제공하는 사용자 고유 ID)
- role (USER)
- createdAt

특징:
- OAuth 인증을 통해 생성됨
- 시스템 내 식별 역할만 수행
- 비즈니스 도메인 정보는 포함하지 않음 (피부 상태, 설문 결과 등)
---
## 4. Notes

- 현재는 단일 User 모델을 사용하며, OAuth 정보도 User 내부에 포함한다
- 다중 OAuth 계정 연결이 필요한 경우 userOAuth 분리로 확장 가능하다
- JWT는 인증/인가를 위한 기술 요소이며 도메인에 포함되지 않는다
- 사용자와 피부 분석 데이터는 별도 도메인(SkinAnalysis)에서 관리한다