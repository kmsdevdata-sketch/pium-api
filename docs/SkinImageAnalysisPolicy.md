# 사진 기반 피부 분석 정책

## 1. 목적

사진 기반 피부 분석은 기존 설문 기반 피부 진단의 접근성을 높이기 위한 추가 진입 방식이다.

현재 피움의 피부 진단은 설문 응답을 기반으로 7개 피부 지표를 계산한다.
사진 분석은 사용자가 더 빠르게 진단을 시작할 수 있도록 하되, 사진 한 장으로 모든 피부 상태를 단정하지 않는다.

핵심 원칙:

```text
입력 방식은 설문과 사진으로 나뉠 수 있다.
하지만 최종 출력은 동일한 SkinMetricScore 7축으로 통일한다.
```

사진 분석 결과도 최종적으로 `SkinAnalysisResult`로 저장하고, 기존 결과 조회와 상품 추천 흐름을 그대로 재사용한다.

## 2. 분석 범위

현재 피부 지표는 다음 7개다.

```text
DRYNESS             // 건조 상태
BARRIER             // 피부 장벽 상태
OILINESS            // 유분 피지 경향
BLEMISH_PRONENESS   // 트러블 발생 경향 / 피부 트러블 민감도
SENSITIVITY          // 민감성 / 자극 민감 반응
PIGMENTATION_TONE   // 색소침착 피부 톤 상태
AGING_SIGNS         // 주름 탄력 등 노화 징후
```

사진으로 상대적으로 관찰하기 쉬운 영역:

| 지표 | 사진 분석 역할 | 이유 |
| --- | --- | --- |
| `BLEMISH_PRONENESS` | 주요 신호 | 현재 보이는 트러블, 붉은 병변, 흔적을 관찰할 수 있음 |
| `PIGMENTATION_TONE` | 주요 신호 | 톤 불균일, 잡티, 색소 신호를 관찰할 수 있음 |
| `AGING_SIGNS` | 주요 신호 | 주름, 탄력 저하로 보이는 표면 신호를 일부 관찰할 수 있음 |

사진만으로 확정하기 어려운 영역:

| 지표 | 사진 분석 역할 | 보정 방식 |
| --- | --- | --- |
| `DRYNESS` | 보조 신호 | 세안 후 당김, 각질 들뜸 문항으로 보정 |
| `OILINESS` | 보조 신호 | 오후 번들거림 문항으로 보정 |
| `SENSITIVITY` | 보조 신호 | 새 화장품 사용 후 따가움/붉어짐 문항으로 보정 |
| `BARRIER` | 직접 분석하지 않음 | 기존처럼 건조/민감 신호에서 파생 |

## 3. 사용자 플로우

사진 분석은 촬영 즉시성을 살리기 위해 사용자가 그 자리에서 셀카를 촬영하는 방식을 기본으로 한다.
초기 MVP에서는 앨범 업로드를 제공하지 않는다.

앨범 업로드를 초기 범위에서 제외하는 이유:

- 얼굴이 충분히 보이지 않는 감성 사진이 들어올 가능성이 높다.
- 필터, 보정, 오래된 사진, 조명 차이를 통제하기 어렵다.
- 사진 분석 결과의 신뢰도가 낮아질 수 있다.

권장 플로우:

```text
홈 또는 진단 시작 화면
-> 사진으로 분석하기
-> 촬영 가이드
-> 사진 분석 안내 및 동의
-> 카메라 촬영
-> 이미지 품질 확인
-> 백엔드에 사진 선분석 요청
-> 백엔드가 OpenAI 이미지 분석을 비동기로 시작
-> 보조 문항 4개 + goal 문항 1개
-> 보조 문항 제출 시 이미지 분석 세션 상태 확인
-> 이미지 분석 완료 시 사진 신호와 보조 문항 융합
-> 이미지 분석 진행 중이면 잠시 후 재요청 안내
-> SkinAnalysisResult 저장
-> 기존 결과 화면
-> 기존 추천 화면
```

## 4. 촬영 가이드

카메라를 열기 전 별도 가이드 화면을 둔다.
네이티브 카메라 화면 위에 직접 오버레이를 얹는 방식은 플랫폼 제약이 있을 수 있으므로, MVP에서는 촬영 전 가이드 화면을 우선한다.

가이드 문구:

```text
정면을 바라봐 주세요.
밝은 곳에서 촬영해 주세요.
필터나 보정은 꺼주세요.
마스크와 안경은 벗어주세요.
머리카락이 얼굴을 가리지 않게 해주세요.
가능하면 진한 화장은 피해주세요.
```

MVP에서는 사진 품질 문제를 과도하게 재촬영으로 돌리지 않는다.
사용자가 사진을 여러 번 다시 찍게 되면 진입성이 떨어지므로, 대부분의 품질 이슈는 confidence 보정으로 처리한다.

서버에서 즉시 거절하는 기본 파일 조건:

- 이미지 파일이 아님
- 파일 용량이 제한을 초과함
- 이미지 디코딩에 실패함
- 해상도가 최소 기준보다 낮음

이미지 분석 모델이 확인하는 품질 신호:

- 얼굴이 정면으로 충분히 보이지 않음
- 얼굴이 너무 작거나 잘림
- 조명이 너무 어둡거나 역광이 강함
- 필터, 보정, 스티커가 의심됨
- 마스크, 선글라스, 머리카락으로 주요 부위가 가려짐
- 여러 명이 함께 있음
- 진한 화장으로 피부 표면 판단이 어려움

위 신호 중 대부분은 재촬영 요청이 아니라 사진 신호 confidence를 낮추는 데 사용한다.

재촬영은 분석 자체가 불가능한 경우에만 요청한다.

재촬영 요청 조건:

- 얼굴이 보이지 않음
- 얼굴이 너무 작거나 심하게 잘림
- 여러 명이 함께 있음
- 사진이 심하게 흐림
- 조명이 극도로 어두워 피부가 거의 보이지 않음
- 이미지 파일이 손상되어 분석할 수 없음

재촬영 안내 문구:

```text
정확한 분석을 위해 얼굴이 잘 보이는 정면 사진이 필요해요.
밝은 곳에서 필터 없이 다시 촬영해 주세요.
```

품질은 아쉽지만 분석 가능한 경우의 안내 문구:

```text
사진 상태에 따라 일부 지표는 보수적으로 반영했어요.
```

## 5. 보조 문항

사진 분석 후 정확한 진단을 위해 짧은 보조 문항을 진행한다.
목표는 기존 11문항 설문을 반복하는 것이 아니라, 사진으로 보기 어려운 체감 신호를 보정하는 것이다.

보조 문항은 4개 지표 문항과 1개 goal 문항으로 구성한다.

| 목적 | 질문 |
| --- | --- |
| 건조 보정 | 세안 후 1시간 안에 얼굴이 당기거나 각질이 들뜨는 편인가요? |
| 유분 보정 | 오후가 되면 이마·코 주변이나 얼굴 전체가 번들거리는 편인가요? |
| 민감성 보정 | 새 화장품을 쓰면 따갑거나 붉어지는 편인가요? |
| 트러블 보정 | 최근 2주 동안 트러블이 새로 올라오거나 반복되는 편인가요? |
| 목표 선택 | 지금 가장 신경 쓰이는 피부 고민은 무엇인가요? |

### 5.1 지표 문항 선택지

지표 보정 문항은 5개 선택지를 고정한다.

```text
전혀 아니에요
드물게 그래요
가끔 그래요
자주 그래요
거의 항상 그래요
```

단일 문항 응답은 순서형 데이터에 가깝기 때문에, 한 문항만으로 0점 또는 100점을 부여하지 않는다.
문항 점수는 다음처럼 압축 점수로 변환한다.

```text
전혀 아니에요      -> 15
드물게 그래요      -> 35
가끔 그래요        -> 55
자주 그래요        -> 75
거의 항상 그래요   -> 90
```

이 점수는 최종 피부 점수가 아니라 사진 신호와 융합하기 위한 보정 신호다.

### 5.2 Goal 선택지

goal은 피부 지표 점수에 직접 반영하지 않는다.
기존 추천 구조와 동일하게 `SkinAnalysisResult`에 저장한 뒤 추천 단계에서 `goalBoostTraits`로만 사용한다.

최대 2개 선택을 유지한다.

```text
수분·장벽 안정
진정·자극 완화
트러블·피지 관리
톤·잡티 케어
탄력·주름 케어
```

추천 trait 매핑:

| Goal | 추천 boost trait |
| --- | --- |
| 수분·장벽 안정 | `HYDRATION_SUPPORT`, `BARRIER_SUPPORT` |
| 진정·자극 완화 | `SOOTHING_SUPPORT`, `BARRIER_SUPPORT` |
| 트러블·피지 관리 | `BLEMISH_CARE_SUPPORT`, `SEBUM_CONTROL_SUPPORT` |
| 톤·잡티 케어 | `BRIGHTENING_SUPPORT`, `UV_PROTECTION` |
| 탄력·주름 케어 | `ANTI_AGING_SUPPORT`, `UV_PROTECTION` |

## 6. 사진 신호와 문항 융합

사진 분석 모델은 최종 7개 지표를 단정해서 반환하지 않는다.
먼저 관찰 가능한 visual signal과 confidence를 반환하고, 서버의 융합 정책이 최종 점수를 만든다.

이미지 품질이 완벽하지 않더라도 분석 가능한 수준이면 결과를 저장한다.
이 경우 품질 신호를 바탕으로 사진 가중치를 낮추고, 보조 문항과 보수 기준값의 비중을 높인다.

모델 출력 예시:

```json
{
  "imageQuality": {
    "usable": true,
    "faceVisible": true,
    "lighting": "GOOD",
    "makeupOrFilterSuspected": false,
    "reasons": []
  },
  "visualSignals": {
    "blemish": { "score": 68, "confidence": "MEDIUM" },
    "pigmentationTone": { "score": 52, "confidence": "MEDIUM" },
    "agingSigns": { "score": 35, "confidence": "LOW" },
    "drynessHint": { "score": 42, "confidence": "LOW" },
    "oilinessHint": { "score": 28, "confidence": "LOW" },
    "rednessHint": { "score": 45, "confidence": "LOW" }
  },
  "warnings": [
    "사진 조명에 따라 유분과 톤 판단이 달라질 수 있어요."
  ]
}
```

초기 가중치:

```text
DRYNESS
= question 70% + imageDrynessHint 30%

OILINESS
= question 70% + imageOilinessHint 30%

SENSITIVITY
= question 75% + imageRednessHint 25%

BLEMISH_PRONENESS
= imageBlemish 70% + question 30%

PIGMENTATION_TONE
= imagePigmentationTone 85% + conservativeBaseline 15%

AGING_SIGNS
= imageAgingSigns 85% + conservativeBaseline 15%

BARRIER
= DRYNESS + SENSITIVITY 기반 파생
```

`PIGMENTATION_TONE`, `AGING_SIGNS`는 MVP에서 별도 문항을 두지 않는다.
나머지 15%는 사진 상세 정보를 저장하지 않고 서버 내부의 보수 기준값으로 채운다.
추후 보정 문항 또는 사용자 피드백 데이터가 생기면 이 영역을 교체할 수 있다.

### 6.1 Confidence 보정

사진 신호의 confidence가 낮으면 사진 가중치를 낮춘다.

```text
HIGH   -> 사진 비중 그대로 사용
MEDIUM -> 사진 비중 * 0.75
LOW    -> 사진 비중 * 0.4
```

사진 가중치가 낮아진 만큼 문항 신호 또는 기본 보수값의 비중을 높인다.

## 7. 모델 사용 정책

모델은 OpenAI vision-capable 모델을 사용한다.

초기 운영 원칙:

```text
1차: GPT-5 Nano
품질 보강 필요 시: GPT-5 Mini
```

모델 선택 기준:

- `GPT-5 Nano`로 충분히 안정적인 visual signal이 나오면 Nano를 유지한다.
- 트러블, 톤, 주름 신호의 일관성이 낮거나 재촬영/오분류가 많으면 Mini를 검토한다.
- 모델은 사용자에게 직접 설명문을 생성하지 않고, 구조화된 visual signal만 생성한다.
- 최종 점수와 저장은 서버 정책으로 수행한다.

## 8. API와 호출 구조

OpenAI API는 프론트에서 직접 호출하지 않는다.
프론트는 사진 촬영과 사용자 확인만 담당하고, 백엔드가 모델 호출, 검증, 융합, 저장을 담당한다.

권장 구조:

```text
프론트
-> 카메라 촬영
-> 이미지 파일을 백엔드로 먼저 전송
-> 백엔드가 이미지 분석 세션을 만들고 OpenAI 호출을 비동기로 시작
-> 프론트에 analysisSessionId 반환
-> 프론트는 사용자가 보조 문항 + goals를 답하게 함
-> analysisSessionId + 보조 문항 + goals를 백엔드로 전송
-> 백엔드가 이미지 분석 세션 상태 확인
-> 이미지 분석 완료 시 응답 검증
-> 백엔드가 보조 문항과 융합
-> 백엔드가 SkinAnalysisResult 저장
-> 프론트에 AnalyzeResultView 반환
```

프론트에서 OpenAI를 직접 호출하지 않는 이유:

- API Key가 클라이언트에 노출된다.
- 사용자가 모델 응답 또는 점수를 조작해 백엔드에 보낼 수 있다.
- 이미지 품질 검증, schema 검증, 점수 융합 정책이 우회될 수 있다.
- 얼굴 이미지 처리와 폐기 정책을 서버에서 통제하기 어렵다.

권장 API:

```http
POST /api/v1/skin-images/pre-analyze
Content-Type: multipart/form-data
Authorization: Bearer ...

image: file
```

응답:

```json
{
  "analysisSessionId": "uuid",
  "questions": [],
  "goalQuestion": {}
}
```

`pre-analyze`는 사진 원본을 저장하지 않고 OpenAI 분석 작업만 시작한다.
응답의 `analysisSessionId`는 이후 보조 문항 제출 시 이미지 분석 결과를 찾기 위한 짧은 TTL의 임시 식별자다.

최종 분석 API:

```http
POST /api/v1/skin-images/analyze
Content-Type: application/json
Authorization: Bearer ...
```

```json
{
  "analysisSessionId": "uuid",
  "answers": [
    {
      "questionId": "IMG_DRYNESS_1",
      "selectedOptionCodes": ["IMG_DRYNESS_3"]
    }
  ],
  "goals": ["Q11_1", "Q11_2"]
}
```

최종 분석 요청 시 이미지는 다시 보내지 않는다.
백엔드는 `analysisSessionId`로 서버 임시 저장소를 조회한 뒤 사진 신호와 보조 문항을 융합한다.

응답 케이스:

```text
COMPLETED
- 이미지 분석이 완료된 상태
- 백엔드가 최종 점수 융합과 저장을 수행
- 기존 결과 화면에서 사용할 AnalyzeResultView 반환

PROCESSING
- 이미지 분석이 아직 완료되지 않은 상태
- 저장하지 않고 fallback 응답 반환
- 프론트는 retryAfterSeconds 이후 동일 payload로 재요청

SESSION_EXPIRED
- TTL 만료, 서버 재시작, 세션 누락 등으로 이미지 분석 세션을 찾을 수 없는 상태
- 프론트는 다시 촬영 흐름으로 이동

IMAGE_UNANALYZABLE
- 얼굴이 보이지 않거나 분석 자체가 불가능한 사진
- 저장하지 않고 재촬영 안내

FAILED
- OpenAI 호출 실패 또는 구조화 응답 검증 실패
- 저장하지 않고 재시도 안내
```

설문 분석 API와 사진 분석 API는 분리한다.
`/api/v1/surveys/analyze`에 `type: survey | image` 분기를 넣지 않는다.

분리 이유:

- 요청 형식이 다르다.
- validation 규칙이 다르다.
- 설문 분석 서비스가 이미지 분석 분기 관리자처럼 커지는 것을 피한다.
- 저장 이후 결과/추천 흐름은 동일하게 재사용할 수 있다.

## 9. 개인정보와 이미지 저장 정책

얼굴 사진은 민감도가 높은 입력이므로 보수적으로 다룬다.

MVP 원칙:

```text
이미지 원본은 저장하지 않는다.
이미지 파일 경로를 저장하지 않는다.
이미지 base64를 로그로 남기지 않는다.
이미지 품질 정보, 모델 raw response, visual signal 원문도 저장하지 않는다.
```

서버 처리 흐름:

```text
이미지 수신
-> 이미지 분석 세션 생성
-> 메모리 또는 임시 스트림으로 OpenAI 요청
-> OpenAI 요청 완료 후 이미지 원본 폐기
-> 구조화 응답을 짧은 TTL의 서버 임시 저장소에 보관
-> 보조 문항 제출 시 임시 저장소에서 구조화 응답 조회
-> 서버 정책으로 최종 점수 산출
-> SkinAnalysisResult 저장
-> 최종 저장 후 이미지 분석 세션 폐기
```

임시 저장소는 Caffeine 같은 인메모리 TTL 캐시를 우선 사용한다.
MVP에서는 Redis나 DB 임시 테이블을 도입하지 않는다.
캐시에 이미지 원본, 이미지 URL, base64, OpenAI raw response를 저장하지 않는다.
캐시에 저장 가능한 값은 `analysisSessionId`, `userId`, 분석 상태, 짧은 TTL의 구조화 이미지 분석 결과로 제한한다.
`analysisSessionId` 조회 시 현재 인증 사용자와 세션의 `userId`가 일치해야 한다.

DB에 저장하는 것은 최종 결과에 필요한 최소 데이터로 제한한다.

저장 대상:

- `SkinAnalysisResult`
- 7개 `SkinMetricScore`
- 사용자가 선택한 `goals`
- `analysisType = IMAGE`

저장하지 않는 대상:

- 얼굴 사진 원본
- 얼굴 사진 URL
- 이미지 EXIF
- 이미지 품질 상세값
- OpenAI raw response
- visual signal 원문
- 모델 confidence 상세값

## 10. 기존 구조 재사용 원칙

사진 분석은 입력과 엔진만 새로 둔다.
그 이후 흐름은 기존 설문 분석 결과와 동일하게 유지한다.

재사용 대상:

| 영역 | 재사용 여부 |
| --- | --- |
| `AnalyzedSkinMetrics` | 재사용 |
| `SkinMetricScore` | 재사용 |
| `SkinAnalysisResult` | 재사용 |
| `SaveSkinAnalysisResultPort` | 재사용 |
| `SkinAnalysisResultPersistenceAdapter` | 재사용 |
| 결과 조회 API | 재사용 |
| 결과 해석 문장 composer | 재사용 |
| 추천 해석 `SkinInterpreter` | 재사용 |
| 추천 점수화 `RecommendationPolicy` | 재사용 |
| goal boost 정책 | 재사용 |

새로 둘 영역:

```text
adapter/inbound/web/skinanalysis/image
application/skinanalysis/image
domain/skinanalysis/image
adapter/outbound/skinanalysis/image/openai
```

권장 application 흐름:

```text
AnalyzeImageSkinAnalysisService
-> OpenAiSkinImageAnalyzerPort
-> ImageSkinAnalysisEngine
-> ImageSurveyFusionPolicy
-> AnalyzedSkinMetrics
-> SkinAnalysisResult.create(userId, scores, goals, analysisType)
-> SaveSkinAnalysisResultPort
```

현재 `SkinAnalysisEngine`은 입력 타입이 `NormalizeSurveySubmission`이라 사실상 설문 전용이다.
사진 분석을 여기에 억지로 넣지 않는다.
향후 명확성을 위해 설문 엔진 이름을 `SurveySkinAnalysisEngine` 또는 `DefaultSurveySkinAnalysisEngine`으로 정리할 수 있다.

## 11. 마이그레이션 정책

사진 분석 도입 시점에 `skin_analysis_result`에 분석 타입을 추가한다.

권장 컬럼:

```sql
analysis_type VARCHAR(32) NOT NULL DEFAULT 'SURVEY'
```

값:

```text
SURVEY
IMAGE
```

기존 데이터는 모두 `SURVEY`로 본다.

목적:

- 진단 기록에서 설문 기반/사진 기반 결과를 구분할 수 있다.
- 운영 중 사진 분석 품질을 설문 분석과 분리해 볼 수 있다.
- 향후 `HYBRID` 분석을 도입할 여지를 남긴다.

단, 이미지 자체나 사진 상세 메타데이터를 저장하기 위한 컬럼은 만들지 않는다.

## 12. 사용자 커뮤니케이션 정책

사진 분석은 의학적 진단처럼 표현하지 않는다.

사용 가능한 표현:

```text
사진에서 트러블 신호가 비교적 뚜렷하게 보여요.
사진만으로 확인하기 어려운 지표는 추가 문항을 함께 반영했어요.
피부 상태 선택에 참고해 주세요.
```

피해야 할 표현:

```text
여드름입니다.
색소침착 질환입니다.
피부 장벽이 손상됐습니다.
치료가 필요합니다.
반드시 이 제품을 써야 합니다.
```

기본 안내:

```text
사진 분석은 피부 상태 참고용이며 의학적 진단이 아니에요.
피부 반응은 개인마다 다를 수 있어요.
```

## 13. 근거 메모

이 정책은 다음 전제를 따른다.

- 건조는 당김, 거칠음, 각질/벗겨짐 같은 체감 및 표면 신호로 나타날 수 있다.
- 유분은 번들거림, 광택, 피지 과다 체감과 연결된다.
- 민감성은 제품 사용 후 따가움, 화끈거림, 붉어짐 같은 반응성이 중요하다.
- 트러블은 현재 보이는 병변뿐 아니라 최근 반복성도 함께 봐야 한다.
- 5점 척도 응답은 순서형 데이터 성격이 강하므로 단일 문항을 0~100 절대 점수로 직접 해석하지 않는다.

참고:

- AAD, dry skin symptoms: https://www.aad.org/public/diseases/a-z/dry-skin-symptoms
- AAD, oily skin care tips: https://www.aad.org/public/everyday-care/skin-care-basics/dry/oily-skin
- Cleveland Clinic, sensitive skin: https://my.clevelandclinic.org/health/diseases/21180-sensitive-skin
- AAD, acne basics: https://www.aad.org/public/diseases/acne/causes/acne-causes
- Likert scale overview: https://www.scribbr.com/methodology/likert-scale/
- Survey design and Likert midpoint: https://boisestate.pressbooks.pub/surveydesign/chapter/4-2/

## 14. 결론

사진 분석은 기존 설문 분석을 대체하는 기능이 아니라, 더 빠른 진입을 위한 별도 분석 방식이다.

최종 설계 방향:

```text
사진 촬영은 프론트가 담당한다.
OpenAI 호출과 점수 융합은 백엔드가 담당한다.
사진 촬영 직후 백엔드가 이미지 선분석을 비동기로 시작한다.
프론트는 보조 문항 제출 시 analysisSessionId만 전달한다.
이미지 분석이 완료되지 않았으면 백엔드는 fallback 응답을 반환하고 프론트는 재요청한다.
이미지는 저장하지 않고 즉시 폐기한다.
사진 상세 정보도 저장하지 않는다.
분석 타입만 저장한다.
최종 결과는 기존 SkinAnalysisResult로 통일한다.
추천은 기존 goal boost와 RecommendationPolicy를 그대로 재사용한다.
```
