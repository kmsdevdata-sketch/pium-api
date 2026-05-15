package com.pium.domain.skinanalysis.engine;

import com.pium.application.skinanalysis.analyze.required.dto.AnalyzedSkinMetrics;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;

import java.util.List;

/**
 * 내부 계산 결과를 도메인 표준 표현인 {@link SkinMetricScore} 리스트로 조립한다.
 */
class SkinMetricScoreAssembler {

    /**
     * 직접 계산된 6개 축과 파생 BARRIER 점수를 7축 점수 리스트로 변환한다.
     *
     * @param bundle 직접 계산된 점수 묶음
     * @param barrierScore 파생 장벽 점수
     * @return 도메인 표준 점수 리스트
     */
    AnalyzedSkinMetrics assemble(MetricScoreBundle bundle, int barrierScore) {
        return new AnalyzedSkinMetrics(List.of(
                SkinMetricScore.of(SkinMetric.DRYNESS, bundle.dryness()),
                SkinMetricScore.of(SkinMetric.BARRIER, barrierScore),
                SkinMetricScore.of(SkinMetric.OILINESS, bundle.oiliness()),
                SkinMetricScore.of(SkinMetric.BLEMISH_PRONENESS, bundle.blemishProneness()),
                SkinMetricScore.of(SkinMetric.SENSITIVITY, bundle.sensitivity()),
                SkinMetricScore.of(SkinMetric.PIGMENTATION_TONE, bundle.pigmentationTone()),
                SkinMetricScore.of(SkinMetric.AGING_SIGNS, bundle.agingSigns())
        ));
    }
}
