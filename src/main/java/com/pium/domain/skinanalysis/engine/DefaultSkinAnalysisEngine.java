package com.pium.domain.skinanalysis.engine;

import com.pium.application.skinanalysis.analyze.required.dto.AnalyzedSkinMetrics;
import com.pium.application.skinanalysis.analyze.required.dto.NormalizeSurveySubmission;
import com.pium.domain.skinanalysis.SkinAnalysisEngine;

/**
 * SkinAnalysis 엔진 파사드.
 */
public class DefaultSkinAnalysisEngine implements SkinAnalysisEngine {

    private final SurveyAnswerCollector surveyAnswerCollector;
    private final SkinMetricScoreCalculator skinMetricScoreCalculator;
    private final BarrierScoreDeriver barrierScoreDeriver;
    private final SkinMetricScoreAssembler skinMetricScoreAssembler;

    public DefaultSkinAnalysisEngine() {
        this(
                new SurveyAnswerCollector(),
                new SkinMetricScoreCalculator(),
                new BarrierScoreDeriver(),
                new SkinMetricScoreAssembler()
        );
    }

    DefaultSkinAnalysisEngine(
            SurveyAnswerCollector surveyAnswerCollector,
            SkinMetricScoreCalculator skinMetricScoreCalculator,
            BarrierScoreDeriver barrierScoreDeriver,
            SkinMetricScoreAssembler skinMetricScoreAssembler
    ) {
        this.surveyAnswerCollector = surveyAnswerCollector;
        this.skinMetricScoreCalculator = skinMetricScoreCalculator;
        this.barrierScoreDeriver = barrierScoreDeriver;
        this.skinMetricScoreAssembler = skinMetricScoreAssembler;
    }

    @Override
    public AnalyzedSkinMetrics analyze(NormalizeSurveySubmission submission) {
        AnalysisContext context = surveyAnswerCollector.collect(submission);
        MetricScoreBundle metricScoreBundle = skinMetricScoreCalculator.calculate(context);
        int barrierScore = barrierScoreDeriver.derive(context, metricScoreBundle);
        return skinMetricScoreAssembler.assemble(metricScoreBundle, barrierScore);
    }
}
