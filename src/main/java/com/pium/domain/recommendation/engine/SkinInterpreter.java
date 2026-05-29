package com.pium.domain.recommendation.engine;

import com.pium.domain.recommendation.enumtype.MetricLevel;
import com.pium.domain.recommendation.exception.RecommendationErrorCode;
import com.pium.domain.recommendation.exception.RecommendationException;
import com.pium.domain.recommendation.model.SkinInterpretation;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;

import java.util.Map;
import java.util.Objects;

/**
 * SkinAnalysisResult를 추천 전용 중간 해석 모델인 SkinInterpretation으로 변환한다.
 *
 * <p>이 클래스는 상품을 조회하거나 점수화하지 않는다.
 * 피부 상태 해석 정책들을 조합해 추천 후보 생성 전 단계의 의미 모델만 만든다.</p>
 */
public class SkinInterpreter {

    private final SkinMetricLevelReader levelReader;
    private final MetricNeedPolicy metricNeedPolicy;
    private final MetricRiskPolicy metricRiskPolicy;
    private final GoalNeedPolicy goalNeedPolicy;
    private final RoutineIntentPolicy routineIntentPolicy;

    public SkinInterpreter() {
        this(
                new SkinMetricLevelReader(),
                new MetricNeedPolicy(),
                new MetricRiskPolicy(),
                new GoalNeedPolicy(),
                new RoutineIntentPolicy()
        );
    }

    public SkinInterpreter(
            SkinMetricLevelReader levelReader,
            MetricNeedPolicy metricNeedPolicy,
            MetricRiskPolicy metricRiskPolicy,
            GoalNeedPolicy goalNeedPolicy,
            RoutineIntentPolicy routineIntentPolicy
    ) {
        if (
                levelReader == null ||
                metricNeedPolicy == null ||
                metricRiskPolicy == null ||
                goalNeedPolicy == null ||
                routineIntentPolicy == null
        ) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_RECOMMENDATION_POLICY);
        }
        this.levelReader = levelReader;
        this.metricNeedPolicy = metricNeedPolicy;
        this.metricRiskPolicy = metricRiskPolicy;
        this.goalNeedPolicy = goalNeedPolicy;
        this.routineIntentPolicy = routineIntentPolicy;
    }

    /**
     * 피부 분석 결과와 goal을 추천 엔진이 읽을 수 있는 해석 결과로 변환한다.
     */
    public SkinInterpretation interpret(SkinAnalysisResult result) {
        if (Objects.isNull(result)) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_RECOMMENDATION_POLICY);
        }

        Map<SkinMetric, MetricLevel> levels = levelReader.read(result.getSkinMetricScores());

        return new SkinInterpretation(
                result.getId().value(),
                routineIntentPolicy.resolve(levels),
                metricNeedPolicy.primaryNeeds(levels),
                metricNeedPolicy.secondaryNeeds(levels),
                metricRiskPolicy.constraints(levels),
                goalNeedPolicy.goalNeeds(result.getGoals(), levels),
                goalNeedPolicy.conflictNotices(result.getGoals(), levels)
        );
    }
}
