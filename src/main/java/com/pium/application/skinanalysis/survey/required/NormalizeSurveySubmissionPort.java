package com.pium.application.skinanalysis.survey.required;

import com.pium.application.skinanalysis.survey.provided.dto.AnalyzeCommand;
import com.pium.application.skinanalysis.survey.required.dto.NormalizeSurveySubmission;

/**
 * Required Port
 * - 외부 설문 응답(AnalyzeCommand)을 도메인 엔진 입력용 표준 계약으로 변환
 */
public interface NormalizeSurveySubmissionPort {

    /**
     * 설문 응답 정규화
     *
     * @param command 원본 설문 응답
     * @return 내부 표준화된 설문 제출 모델
     */
    NormalizeSurveySubmission normalize(AnalyzeCommand command);
}
