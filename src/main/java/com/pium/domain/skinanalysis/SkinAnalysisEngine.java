package com.pium.domain.skinanalysis;

import com.pium.application.skinanalysis.survey.required.dto.NormalizeSurveySubmission;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;

/**
 * Domain 엔진 포트
 * - 정규화된 설문 입력을 받아 상태 점수를 계산하고 결과 aggregate를 생성
 */
public interface SkinAnalysisEngine {

    /**
     * 상태 분석 실행
     *
     * @param submission 정규화된 설문 입력
     * @return 분석 결과 aggregate
     */
    SkinAnalysisResult analyze(NormalizeSurveySubmission submission);
}
