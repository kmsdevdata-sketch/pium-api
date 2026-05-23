## 피움 Pium · Backend API
> 토스 미니앱(앱인토스)으로 실운영 중인 피부타입 분석 서비스의 백엔드입니다.
> 
> 팀 프로젝트를 혼자 재설계하였고, 해당 레포지토리는 전적으로 제 작업물만 담고 있습니다
> 
> 원본 팀 프로젝트 레포지토리 -> [skin-service](https://github.com/swyp-3team/skin-service.git)
---
## Live Service

- 서비스 링크: [피움 바로가기](https://minion.toss.im/zge4f54D)
- 참고: 모바일 환경에서 열어야 정상적으로 확인 가능합니다.
- 실행 환경: Toss 앱인토스 미니앱
- 백엔드 배포: DigitalOcean, Docker, Nginx, HTTPS

> 현재 클라이언트는 Toss 앱인토스 WebView로 제공하고 있지만,  
> 백엔드는 클라이언트 종류에 의존하지 않는 REST API로 구성했습니다.

<p align="center">
  <img src="docs/assets/pium-home.PNG" width="240" alt="피움 홈 화면" />
  <img src="docs/assets/pium-survey.png" width="240" alt="피움 설문 화면" />
  <img src="docs/assets/pium-result.png" width="240" alt="피움 결과 화면" />
</p>

---
## Core Feature
### 규칙 기반 피부 상태 분석 
피움의 피부 분석은 AI API에 의존하지 않고,  
설문 응답을 도메인 규칙으로 해석해 피부 상태를 계산합니다. 
```text
Survey Answer
  사용자가 제출한 설문 응답

-> Normalize
  내부 분석 엔진이 읽을 수 있는 표준 입력으로 정규화

-> Question Rule Matching
  문항 ID와 선택지 코드를 기준으로 점수 규칙 매칭

-> Metric Score Calculation
  건조·유분·트러블·민감·톤·탄력 6개 직접 지표 계산

-> Barrier Score Derivation
  건조/민감 신호를 기반으로 장벽 지표 파생

-> SkinAnalysisResult
  7축 피부 상태 결과 저장

-> State Labeling
  점수를 LOW / MID / HIGH 상태 라벨로 변환

-> Result Text Composition
  상태 조합에 따른 한줄 요약과 상세 해석 생성
```

관련 구현:
관련 구현:

- [SurveySubmissionNormalizerAdapter](src/main/java/com/pium/adapter/outbound/skinanalysis/normalizer/SurveySubmissionNormalizerAdapter.java): 설문 응답 정규화
- [QuestionRule](src/main/java/com/pium/domain/skinanalysis/engine/QuestionRule.java): 문항/선택지별 점수 규칙
- [DefaultSkinAnalysisEngine](src/main/java/com/pium/domain/skinanalysis/engine/DefaultSkinAnalysisEngine.java): 분석 엔진 파사드
- [SkinMetricScoreCalculator](src/main/java/com/pium/domain/skinanalysis/engine/SkinMetricScoreCalculator.java): 직접 지표 계산
- [BarrierScoreDeriver](src/main/java/com/pium/domain/skinanalysis/engine/BarrierScoreDeriver.java): 장벽 점수 파생
- [SkinAnalysisResultViewComposer](src/main/java/com/pium/application/skinanalysis/result/service/SkinAnalysisResultViewComposer.java): 결과 라벨링 및 해석 문장 생성

---

## Production
현재 백엔드는 DigitalOcean 서버에서 Docker기반으로 운영 중이며,  
토스 앱인토스 WebView 클라이언트와 HTTPS 통신으로 연동되어 있습니다.

### 배포 파이프라인 
배포 파이프라인은 GitHub Actions에서 CI 성공 후 GHCR 이미지를 빌드하고,  
서버에서 신규 앱 컨테이너를 반대 슬롯에 띄운 뒤 health check가 통과하면  
Nginx upstream을 새포트로 전환하는 방식으로 구성했습니다.  
```text
GitHub Actions CI
-> bootJar / test
-> Docker image build & push to GHCR
-> SSH deploy
-> app-blue / app-green 슬롯 전환 
-> /actuator/health 확인 
-> Nginx upstream reload
```
---

## Architecture
이 프로젝트는 **Adapter** - **Application** - **Domain** 계층을 기준으로 구성했습니다.  
```text
adapter
  ├─ inbound   // Web API, request/response mapping
  └─ outbound  // Persistence, Toss API, JWT

application
  ├─ auth
  ├─ user
  └─ skinanalysis

domain
  ├─ user
  └─ skinanalysis
```
---

## Domain Design

피움의 핵심 흐름은 설문 응답을 피부 상태 벡터로 변환하고,  
이후 추천 도메인이 해당 상태를 소비할 수 있도록 표준화하는 것입니다.  
```text
설문 응답
-> 정규화된 응답 모델
-> SkinMetricScore
-> SkinAnalysisResult
-> 상태 라벨 및 해석 문장
```
---

## Problems & Decisions
### 1. 설문 점수를 그대로 피부 상태로 말할 수 없었다

설문 점수는 물리 단위가 아니기 때문에 `70점`이 `10점`보다 정확히 7배 심각하다고 말할 수 없습니다.  
그래서 점수는 내부 계산 신호로만 사용하고, 사용자에게는 `LOW / MID / HIGH` 상태 라벨과 근거 문장으로 전달했습니다.

### 2. 장벽 상태는 사용자가 직접 인식하기 어렵다

피부 장벽은 직접 문항으로 묻기보다 건조, 민감, 자극 반응 패턴에서 파생하는 신호로 설계했습니다.  
이 `BARRIER` 값은 이후 추천 단계에서 안전 게이트로 사용할 수 있도록 분리했습니다.

### 3. 추천 기능을 고려해 분석 결과의 경계를 먼저 잡았다

상품 추천 기능은 앱인토스 외부 링크/제휴 정책 검토가 필요한 영역이라 아직 구현하지 않았습니다.  
대신 이후 추천 기능이 분석 로직에 직접 의존하지 않도록,  
피부 분석 결과를 `SkinMetricScore` 중심의 표준 상태 표현으로 남기도록 설계했습니다.

이후 추천 기능은 이 상태 표현을 입력으로 받아  
상품의 `ProductProfile`과 비교하는 별도 계층으로 확장할 수 있습니다.

### 4. Toss 로그인 특수성을 인증 도메인 밖으로 격리했다

Toss 로그인은 authorizationCode, referrer, mTLS 등 플랫폼 특성이 있지만,  
서비스 내부에서는 provider 기반 외부 사용자 식별 결과만 다루도록 분리했습니다.  
로그인 성공 후 프론트에는 외부 provider 토큰이 아닌 자체 JWT만 반환합니다.
---

## Documents

| 문서 | 내용 |
| --- | --- |
| [Domain Overview](docs/domain/domain-overview.md) | 전체 도메인 분리 기준 |
| [SkinAnalysis Domain](docs/domain/SkinAnalysisDomain.md) | 피부 분석 도메인 책임과 경계 |
| [Product Domain](docs/domain/ProductDomain.md) | 상품/성분 도메인 설계 |
| [Recommendation Domain](docs/domain/RecommendationDomain.md) | 추천 도메인 확장 방향 |
| [SkinAnalysis 해석 모델](docs/SkinAnalysisEvidenceModelProposal.md) | 점수를 상태로 해석한 이유 |
| [추천 설계 개요](docs/Skin-Recommendation-Overview.md) | 안전성 우선 추천 설계 |
| [Login Policy](docs/Login-policy.md) | Toss 로그인 및 JWT 정책 |
| [Survey](docs/survey.md) | 설문 구성 원칙 |
| [설문 문항집](docs/설문_문항집.md) | 실제 설문 문항과 metric 매핑 |