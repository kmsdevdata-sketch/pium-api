package com.pium.domain.skinanalysis.engine;

import com.pium.application.skinanalysis.analyze.required.dto.AnalyzedSkinMetrics;
import com.pium.application.skinanalysis.analyze.required.dto.NormalizeSurveySubmission;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSkinAnalysisEngineTest {

    private final DefaultSkinAnalysisEngine engine = new DefaultSkinAnalysisEngine();

    @Test
    void analyze_7축점수를_생성한다() {
        NormalizeSurveySubmission submission = new NormalizeSurveySubmission(
                List.of(
                        answer("Q_DRYNESS_1", "Q1_3"),
                        answer("Q_DRYNESS_2", "Q2_3"),
                        answer("Q_OILINESS_1", "Q3_1"),
                        answer("Q_OILINESS_2", "Q4_4"),
                        answer("Q_BLEMISH_1", "Q5_2"),
                        answer("Q_BLEMISH_2", "Q6_3", "Q6_4"),
                        answer("Q_SENSITIVITY_1", "Q7_3"),
                        answer("Q_SENSITIVITY_2", "Q8_2"),
                        answer("Q_PIGMENTATION_1", "Q9_4"),
                        answer("Q_AGING_1", "Q10_2")
                )
        );

        AnalyzedSkinMetrics result = engine.analyze(submission);

        Map<SkinMetric, Integer> scores = result.skinMetricScores().stream()
                .collect(Collectors.toMap(
                        score -> score.metric(),
                        score -> score.score()
                ));

        assertThat(scores).containsEntry(SkinMetric.DRYNESS, 66);
        assertThat(scores).containsEntry(SkinMetric.BARRIER, 61);
        assertThat(scores).containsEntry(SkinMetric.OILINESS, 20);
        assertThat(scores).containsEntry(SkinMetric.BLEMISH_PRONENESS, 64);
        assertThat(scores).containsEntry(SkinMetric.SENSITIVITY, 56);
        assertThat(scores).containsEntry(SkinMetric.PIGMENTATION_TONE, 80);
        assertThat(scores).containsEntry(SkinMetric.AGING_SIGNS, 40);
    }

    @Test
    void analyze_장벽부담패턴이_있으면_barrier가_가산된다() {
        NormalizeSurveySubmission submission = new NormalizeSurveySubmission(
                List.of(
                        answer("Q_DRYNESS_1", "Q1_4"),
                        answer("Q_DRYNESS_2", "Q2_4"),
                        answer("Q_OILINESS_1", "Q3_2"),
                        answer("Q_OILINESS_2", "Q4_4"),
                        answer("Q_BLEMISH_1", "Q5_1"),
                        answer("Q_BLEMISH_2", "Q6_6"),
                        answer("Q_SENSITIVITY_1", "Q7_4"),
                        answer("Q_SENSITIVITY_2", "Q8_4"),
                        answer("Q_PIGMENTATION_1", "Q9_1"),
                        answer("Q_AGING_1", "Q10_1")
                )
        );

        AnalyzedSkinMetrics result = engine.analyze(submission);

        int barrierScore = result.skinMetricScores().stream()
                .filter(score -> score.metric() == SkinMetric.BARRIER)
                .findFirst()
                .orElseThrow()
                .score();

        assertThat(barrierScore).isEqualTo(96);
    }

    private NormalizeSurveySubmission.NormalizedAnswer answer(String questionId, String... optionCodes) {
        return new NormalizeSurveySubmission.NormalizedAnswer(questionId, List.of(optionCodes));
    }
}
