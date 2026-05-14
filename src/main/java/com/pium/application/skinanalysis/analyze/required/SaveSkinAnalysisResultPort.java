package com.pium.application.skinanalysis.analyze.required;

import com.pium.domain.skinanalysis.model.SkinAnalysisResult;

/**
 * Required Port
 * - SkinAnalyze 결과를 영속화하기 위한 저장 포트
 */
public interface SaveSkinAnalysisResultPort {

    /**
     * 분석 결과 저장
     *
     * @param result 분석 결과 aggregate
     */
    void save(SkinAnalysisResult result);
}
