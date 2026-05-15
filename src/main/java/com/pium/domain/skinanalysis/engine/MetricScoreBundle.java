package com.pium.domain.skinanalysis.engine;

/**
 * BARRIER를 제외한 직접 계산 대상 6개 축 점수를 묶는 내부 DTO다.
 *
 * <p>파사드 엔진이 계산 단계와 파생 단계 사이에서 점수 집합을 명확하게 전달하도록 돕는다.</p>
 */
record MetricScoreBundle(
        int dryness,
        int oiliness,
        int blemishProneness,
        int sensitivity,
        int pigmentationTone,
        int agingSigns
) {
}
