package com.pium.domain.skinanalysis.engine;

/**
 * DRYNESS와 SENSITIVITY 기반으로 BARRIER를 파생한다.
 */
class BarrierScoreDeriver {

    private static final String Q_DRYNESS_2 = "Q_DRYNESS_2";
    private static final String Q_SENSITIVITY_1 = "Q_SENSITIVITY_1";
    private static final String Q_SENSITIVITY_2 = "Q_SENSITIVITY_2";

    private static final String DRYNESS_WASHED = "Q2_1";
    private static final String DRYNESS_ALL_DAY = "Q2_4";
    private static final String SENSITIVITY_ALWAYS = "Q7_4";
    private static final String SENSITIVITY_SEASONAL = "Q8_4";

    int derive(AnalysisContext context, MetricScoreBundle bundle) {
        int barrierScore = (int) Math.round((bundle.dryness() + bundle.sensitivity()) / 2.0);

        if (context.hasOption(Q_DRYNESS_2, DRYNESS_WASHED)) {
            barrierScore += 5;
        }

        if (context.hasOption(Q_DRYNESS_2, DRYNESS_ALL_DAY)) {
            barrierScore += 5;
        }

        if (context.hasOption(Q_SENSITIVITY_1, SENSITIVITY_ALWAYS)) {
            barrierScore += 5;
        }

        if (context.hasOption(Q_SENSITIVITY_2, SENSITIVITY_SEASONAL)) {
            barrierScore += 5;
        }

        return clamp(barrierScore);
    }

    private int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
