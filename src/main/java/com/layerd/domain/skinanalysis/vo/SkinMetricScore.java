package com.layerd.domain.skinanalysis.vo;

import com.layerd.domain.skinanalysis.enumtype.SkinMetric;
import com.layerd.domain.skinanalysis.exception.SkinAnalysisErrorCode;
import com.layerd.domain.skinanalysis.exception.SkinAnalysisException;

public record SkinMetricScore(SkinMetric metric, int score) {

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;

    public SkinMetricScore {
        if (metric == null) {
            throw new SkinAnalysisException(SkinAnalysisErrorCode.SKIN_METRIC_REQUIRED);
        }
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new SkinAnalysisException(SkinAnalysisErrorCode.INVALID_SKIN_METRIC_SCORE_RANGE);
        }
    }

    public static SkinMetricScore of(SkinMetric metric, int score) {
        return new SkinMetricScore(metric, score);
    }
}
