package com.pium.domain.skinanalysis.image;

import com.pium.application.skinanalysis.analyze.required.dto.AnalyzedSkinMetrics;
import com.pium.application.skinanalysis.image.dto.AnalyzeImageCommand;
import com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis.Confidence.HIGH;
import static com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis.Confidence.LOW;
import static com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis.Confidence.MEDIUM;
import static org.assertj.core.api.Assertions.assertThat;

class ImageSkinAnalysisEngineTest {

    private final ImageSkinAnalysisEngine engine = new ImageSkinAnalysisEngine();

    @Test
    void analyze_사진신호와_보조문항을_7축점수로_융합한다() {
        ImageSkinAnalysis imageAnalysis = new ImageSkinAnalysis(
                new ImageSkinAnalysis.ImageQuality(true, List.of("NONE")),
                new ImageSkinAnalysis.VisualSignals(
                        signal(80, HIGH),
                        signal(60, MEDIUM),
                        signal(20, LOW),
                        signal(45, MEDIUM),
                        signal(90, LOW),
                        signal(50, HIGH)
                ),
                List.of()
        );

        AnalyzedSkinMetrics result = engine.analyze(
                imageAnalysis,
                List.of(
                        answer("IMG_DRYNESS_1", "IMG_DRYNESS_4"),
                        answer("IMG_OILINESS_1", "IMG_OILINESS_2"),
                        answer("IMG_SENSITIVITY_1", "IMG_SENSITIVITY_5"),
                        answer("IMG_BLEMISH_1", "IMG_BLEMISH_2")
                )
        );

        Map<SkinMetric, Integer> scores = result.skinMetricScores().stream()
                .collect(Collectors.toMap(
                        score -> score.metric(),
                        score -> score.score()
                ));

        assertThat(scores).containsEntry(SkinMetric.DRYNESS, 68);
        assertThat(scores).containsEntry(SkinMetric.BARRIER, 74);
        assertThat(scores).containsEntry(SkinMetric.OILINESS, 42);
        assertThat(scores).containsEntry(SkinMetric.BLEMISH_PRONENESS, 67);
        assertThat(scores).containsEntry(SkinMetric.SENSITIVITY, 80);
        assertThat(scores).containsEntry(SkinMetric.PIGMENTATION_TONE, 51);
        assertThat(scores).containsEntry(SkinMetric.AGING_SIGNS, 30);
    }

    @Test
    void analyze_누락된_문항은_중립점수로_보정한다() {
        ImageSkinAnalysis imageAnalysis = new ImageSkinAnalysis(
                new ImageSkinAnalysis.ImageQuality(true, List.of("LOW_LIGHT")),
                new ImageSkinAnalysis.VisualSignals(
                        signal(70, MEDIUM),
                        signal(40, LOW),
                        signal(40, LOW),
                        null,
                        null,
                        null
                ),
                List.of("사진 상태에 따라 일부 지표는 보수적으로 반영했어요.")
        );

        AnalyzedSkinMetrics result = engine.analyze(imageAnalysis, List.of());

        assertThat(result.skinMetricScores()).hasSize(7);
        assertThat(result.skinMetricScores()).allSatisfy(score ->
                assertThat(score.score()).isBetween(0, 100)
        );
    }

    private ImageSkinAnalysis.VisualSignal signal(int score, ImageSkinAnalysis.Confidence confidence) {
        return new ImageSkinAnalysis.VisualSignal(score, confidence);
    }

    private AnalyzeImageCommand.Answer answer(String questionId, String optionCode) {
        return new AnalyzeImageCommand.Answer(questionId, List.of(optionCode));
    }
}
