package com.pium.domain.recommendation.engine;

import com.pium.domain.recommendation.enumtype.MetricLevel;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 피부 분석 점수를 추천 정책에서 사용하는 LOW/MID/HIGH 레벨로 변환한다.
 */
public class SkinMetricLevelReader {

    private static final int MID_MIN_SCORE = 35;
    private static final int HIGH_MIN_SCORE = 70;

    /**
     * 피부 지표 점수 목록을 지표별 레벨 맵으로 변환한다.
     */
    public Map<SkinMetric, MetricLevel> read(List<SkinMetricScore> scores) {
        validateScores(scores);

        Map<SkinMetric, MetricLevel> levels = new EnumMap<>(SkinMetric.class);
        for (SkinMetricScore score : scores) {
            if (levels.containsKey(score.metric())) {
                throw new RecommendationException(RecommendationErrorCode.INVALID_SKIN_METRIC_LEVELS);
            }
            levels.put(score.metric(), levelOf(score.score()));
        }

        RecommendationPolicyValidation.validateLevels(levels);
        return levels;
    }

    private void validateScores(List<SkinMetricScore> scores) {
        if (scores == null || scores.isEmpty()) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_SKIN_METRIC_LEVELS);
        }
        if (scores.stream().anyMatch(score -> score == null || score.metric() == null)) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_SKIN_METRIC_LEVELS);
        }
        if (scores.size() != SkinMetric.values().length) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_SKIN_METRIC_LEVELS);
        }

        long distinctMetricCount = scores.stream()
                .map(SkinMetricScore::metric)
                .distinct()
                .count();

        if (distinctMetricCount != Arrays.stream(SkinMetric.values()).count()) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_SKIN_METRIC_LEVELS);
        }
    }

    private MetricLevel levelOf(int score) {
        if (score >= HIGH_MIN_SCORE) {
            return MetricLevel.HIGH;
        }
        if (score >= MID_MIN_SCORE) {
            return MetricLevel.MID;
        }
        return MetricLevel.LOW;
    }
}
