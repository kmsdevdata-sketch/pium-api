package com.pium.application.skinanalysis.analyze.provided;

import com.pium.application.skinanalysis.analyze.dto.AnalyzeCommand;
import com.pium.application.skinanalysis.analyze.dto.AnalyzeResultView;

/**
 * Provided Port
 * - 설문 응답을 받아 SkinAnalysis 결과를 생성하는 유즈케이스 진입 포트
 *
 */
public interface AnalyzeSkinAnalysis {

    /**
     * 설문 응답을 받아 분석을 수행한다.
     *
     * @param command 설문 응답 커맨드
     * @return 분석 결과 뷰
     */
    AnalyzeResultView analyze(AnalyzeCommand command);


}
