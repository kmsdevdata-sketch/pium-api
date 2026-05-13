package com.pium.application.skinanalysis.survey.provided.dto;


import java.util.List;

/**
 * 유즈케이스 출력 뷰
 *
 * @param skinMetricScores 피부 상태 점수 목록
 */
public record AnalyzeResultView(
        List<SkinMetricScoreView> skinMetricScores
) {

    /**
     * SkinMetric 점수 뷰
     *
     * @param metricKey 피부 상태 지표 (ex.DRYNESS,OILINESS)
     * @param score 상태 점수
     */
    public record SkinMetricScoreView(
            String metricKey,
            int score
    ) {
    }

}
