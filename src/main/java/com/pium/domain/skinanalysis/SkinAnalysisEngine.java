package com.pium.domain.skinanalysis;

import com.pium.application.skinanalysis.analyze.required.dto.AnalyzedSkinMetrics;
import com.pium.application.skinanalysis.analyze.required.dto.NormalizeSurveySubmission;

/**
 * Domain 엔진 포트
 * - 정규화된 설문 입력을 받아 상태 점수를 계산한다
 */
public interface SkinAnalysisEngine {

    /**
     * 상태 분석 실행
     *
     * @param submission 정규화된 설문 입력
     * @return 분석된 피부 상태 점수
     */
    AnalyzedSkinMetrics analyze(NormalizeSurveySubmission submission);
}
