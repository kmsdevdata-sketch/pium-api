package com.pium.application.skinanalysis.survey.provided;

import com.pium.application.skinanalysis.survey.provided.dto.AnalyzeCommand;

/**
 * Provided Port
 * - 설문 응답을 받아 SkinAnalysis 결과를 생성하는 유즈케이스 진입 포트
 *
 */
public interface AnalyzeSkinAnalysis {

    AnalyzeResultView analyze(AnalyzeCommand command);


}
