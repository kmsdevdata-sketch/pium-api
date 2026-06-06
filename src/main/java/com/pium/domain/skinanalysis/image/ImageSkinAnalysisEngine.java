package com.pium.domain.skinanalysis.image;

import com.pium.application.skinanalysis.analyze.required.dto.AnalyzedSkinMetrics;
import com.pium.application.skinanalysis.image.dto.AnalyzeImageCommand;
import com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 사진 분석 visual signal과 보조 문항을 7축 SkinMetricScore로 융합한다.
 */
public class ImageSkinAnalysisEngine {

    private static final String Q_DRYNESS = "IMG_DRYNESS_1";
    private static final String Q_OILINESS = "IMG_OILINESS_1";
    private static final String Q_SENSITIVITY = "IMG_SENSITIVITY_1";
    private static final String Q_BLEMISH = "IMG_BLEMISH_1";

    private static final int NEUTRAL_SCORE = 55;
    private static final int CONSERVATIVE_BASELINE = 35;

    public AnalyzedSkinMetrics analyze(
            ImageSkinAnalysis imageAnalysis,
            List<AnalyzeImageCommand.Answer> answers
    ) {
        Map<String, AnalyzeImageCommand.Answer> answerMap = answers.stream()
                .collect(Collectors.toMap(
                        answer -> answer.questionId().trim(),
                        answer -> answer,
                        (existing, ignored) -> existing
                ));

        ImageSkinAnalysis.VisualSignals signals = imageAnalysis.visualSignals();

        int dryness = weightedQuestionAndImage(
                questionScore(answerMap, Q_DRYNESS),
                signal(signals.drynessHint()),
                0.30
        );
        int oiliness = weightedQuestionAndImage(
                questionScore(answerMap, Q_OILINESS),
                signal(signals.oilinessHint()),
                0.30
        );
        int sensitivity = weightedQuestionAndImage(
                questionScore(answerMap, Q_SENSITIVITY),
                signal(signals.rednessHint()),
                0.25
        );
        int blemish = weightedImageAndQuestion(
                signal(signals.blemish()),
                questionScore(answerMap, Q_BLEMISH),
                0.70
        );
        int pigmentationTone = weightedImageAndBaseline(signal(signals.pigmentationTone()), 0.85);
        int agingSigns = weightedImageAndBaseline(signal(signals.agingSigns()), 0.85);
        int barrier = clamp((int) Math.round((dryness + sensitivity) / 2.0));

        return new AnalyzedSkinMetrics(List.of(
                SkinMetricScore.of(SkinMetric.DRYNESS, dryness),
                SkinMetricScore.of(SkinMetric.BARRIER, barrier),
                SkinMetricScore.of(SkinMetric.OILINESS, oiliness),
                SkinMetricScore.of(SkinMetric.BLEMISH_PRONENESS, blemish),
                SkinMetricScore.of(SkinMetric.SENSITIVITY, sensitivity),
                SkinMetricScore.of(SkinMetric.PIGMENTATION_TONE, pigmentationTone),
                SkinMetricScore.of(SkinMetric.AGING_SIGNS, agingSigns)
        ));
    }

    private int weightedQuestionAndImage(int questionScore, Signal signal, double imageWeight) {
        double adjustedImageWeight = imageWeight * signal.confidenceFactor();
        double questionWeight = 1.0 - adjustedImageWeight;
        return clamp((int) Math.round(questionScore * questionWeight + signal.score() * adjustedImageWeight));
    }

    private int weightedImageAndQuestion(Signal signal, int questionScore, double imageWeight) {
        double adjustedImageWeight = imageWeight * signal.confidenceFactor();
        double questionWeight = 1.0 - adjustedImageWeight;
        return clamp((int) Math.round(signal.score() * adjustedImageWeight + questionScore * questionWeight));
    }

    private int weightedImageAndBaseline(Signal signal, double imageWeight) {
        double adjustedImageWeight = imageWeight * signal.confidenceFactor();
        double baselineWeight = 1.0 - adjustedImageWeight;
        return clamp((int) Math.round(signal.score() * adjustedImageWeight + CONSERVATIVE_BASELINE * baselineWeight));
    }

    private int questionScore(Map<String, AnalyzeImageCommand.Answer> answerMap, String questionId) {
        return Optional.ofNullable(answerMap.get(questionId))
                .flatMap(answer -> answer.selectedOptionCodes().stream().findFirst())
                .map(this::optionScore)
                .orElse(NEUTRAL_SCORE);
    }

    private int optionScore(String optionCode) {
        if (optionCode == null || optionCode.isBlank()) {
            return NEUTRAL_SCORE;
        }
        char last = optionCode.trim().charAt(optionCode.trim().length() - 1);
        return switch (last) {
            case '1' -> 15;
            case '2' -> 35;
            case '3' -> 55;
            case '4' -> 75;
            case '5' -> 90;
            default -> NEUTRAL_SCORE;
        };
    }

    private Signal signal(ImageSkinAnalysis.VisualSignal signal) {
        if (signal == null || signal.confidence() == null) {
            return new Signal(NEUTRAL_SCORE, 0.4);
        }
        return new Signal(clamp(signal.score()), confidenceFactor(signal.confidence()));
    }

    private double confidenceFactor(ImageSkinAnalysis.Confidence confidence) {
        return switch (confidence) {
            case HIGH -> 1.0;
            case MEDIUM -> 0.75;
            case LOW -> 0.4;
        };
    }

    private int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private record Signal(
            int score,
            double confidenceFactor
    ) {
    }
}
