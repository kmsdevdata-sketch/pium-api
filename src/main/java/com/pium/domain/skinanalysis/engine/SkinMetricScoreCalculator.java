package com.pium.domain.skinanalysis.engine;

import java.util.Set;

/**
 * 직접 계산 대상 6개 축 점수를 산출한다.
 */
class SkinMetricScoreCalculator {
    private static final String Q_DRYNESS_1 = "Q_DRYNESS_1";
    private static final String Q_DRYNESS_2 = "Q_DRYNESS_2";
    private static final String Q_OILINESS_1 = "Q_OILINESS_1";
    private static final String Q_OILINESS_2 = "Q_OILINESS_2";
    private static final String Q_BLEMISH_1 = "Q_BLEMISH_1";
    private static final String Q_BLEMISH_2 = "Q_BLEMISH_2";
    private static final String Q_SENSITIVITY_1 = "Q_SENSITIVITY_1";
    private static final String Q_SENSITIVITY_2 = "Q_SENSITIVITY_2";
    private static final String Q_PIGMENTATION_1 = "Q_PIGMENTATION_1";
    private static final String Q_AGING_1 = "Q_AGING_1";

    private static final String OILINESS_DRY_TRIGGER = "Q3_1";
    private static final String OILINESS_T_ZONE = "Q3_3";
    private static final String OILINESS_ALL_FACE = "Q3_4";

    private static final String OILINESS_NONE = "Q4_4";
    private static final String OILINESS_ALWAYS = "Q4_3";

    private static final String BLEMISH_CHEEK = "Q6_3";
    private static final String BLEMISH_ALL_FACE = "Q6_5";
    private static final String BLEMISH_NONE = "Q6_6";

    private static final String DRYNESS_NONE = "Q2_5";

    private static final String SENSITIVITY_FREQUENT = "Q7_3";

    MetricScoreBundle calculate(AnalysisContext context) {
        int dryness = calculateDryness(context);
        int oiliness = calculateOiliness(context);
        int blemishProneness = calculateBlemishProneness(context);
        int sensitivity = calculateSensitivity(context);
        int pigmentationTone = calculatePigmentationTone(context);
        int agingSigns = calculateAgingSigns(context);

        return new MetricScoreBundle(
                dryness,
                oiliness,
                blemishProneness,
                sensitivity,
                pigmentationTone,
                agingSigns
        );
    }

    private int calculateDryness(AnalysisContext context) {
        int intensityScore = scoreOfSingle(context, Q_DRYNESS_1);
        int patternScore = scoreOfSingle(context, Q_DRYNESS_2);

        int weighted = weightedAverage(
                intensityScore, 65,
                patternScore, 35
        );

        if (context.hasOption(Q_OILINESS_1, OILINESS_DRY_TRIGGER)) {
            weighted += 8;
        }

        if (context.hasOption(Q_DRYNESS_2, DRYNESS_NONE) && intensityScore <= 40) {
            weighted -= 8;
        }

        return clamp(weighted);
    }

    private int calculateOiliness(AnalysisContext context) {
        int distributionScore = scoreOfSingle(context, Q_OILINESS_1);
        int triggerScore = scoreOfMulti(context, Q_OILINESS_2, 40, OILINESS_NONE);

        int score = distributionScore + triggerScore;

        if (context.hasOption(Q_OILINESS_1, OILINESS_T_ZONE)) {
            score -= 5;
        }

        if (context.hasOption(Q_OILINESS_1, OILINESS_ALL_FACE)) {
            score += 5;
        }

        if (context.hasOption(Q_OILINESS_2, OILINESS_ALWAYS)) {
            score += 8;
        }

        return clamp(score);
    }

    private int calculateBlemishProneness(AnalysisContext context) {
        int frequencyScore = scoreOfSingle(context, Q_BLEMISH_1);
        int areaScore = scoreOfMulti(context, Q_BLEMISH_2, 30, BLEMISH_NONE);

        int score = frequencyScore + areaScore;

        if (context.hasOption(Q_BLEMISH_2, BLEMISH_CHEEK)) {
            score += 4;
        }

        if (context.hasOption(Q_BLEMISH_2, BLEMISH_ALL_FACE)) {
            score += 6;
        }

        return clamp(score);
    }

    private int calculateSensitivity(AnalysisContext context) {
        int productReactionScore = scoreOfSingle(context, Q_SENSITIVITY_1);
        int environmentReactionScore = scoreOfSingle(context, Q_SENSITIVITY_2);

        int score = weightedAverage(
                productReactionScore, 55,
                environmentReactionScore, 45
        );

        if (context.hasOption(Q_SENSITIVITY_1, SENSITIVITY_FREQUENT)) {
            score += 5;
        }

        return clamp(score);
    }

    private int calculatePigmentationTone(AnalysisContext context) {
        return clamp(scoreOfSingle(context, Q_PIGMENTATION_1));
    }

    private int calculateAgingSigns(AnalysisContext context) {
        return clamp(scoreOfSingle(context, Q_AGING_1));
    }

    /**
     * 단일 선택 문항 점수를 조회한다.
     */
    private int scoreOfSingle(AnalysisContext context, String questionId) {
        QuestionRule rule = QuestionRule.get(questionId);
        if (rule == null) {
            return 0;
        }

        String optionCode = context.firstOptionOf(questionId);
        if (optionCode == null) {
            return 0;
        }

        return rule.optionScores().getOrDefault(optionCode, 0);
    }

    private int scoreOfMulti(
            AnalysisContext context,
            String questionId,
            int maxContribution,
            String zeroIfOnlyOption
    ) {
        QuestionRule rule = QuestionRule.get(questionId);
        if (rule == null) {
            return 0;
        }

        Set<String> optionCodes = context.optionCodesOf(questionId);
        if (optionCodes.isEmpty()) {
            return 0;
        }

        if (optionCodes.size() == 1 && optionCodes.contains(zeroIfOnlyOption)) {
            return 0;
        }

        int total = optionCodes.stream()
                .mapToInt(optionCode -> rule.optionScores().getOrDefault(optionCode, 0))
                .sum();

        return Math.min(total, maxContribution);
    }

    private int weightedAverage(
            int leftScore,
            int leftWeight,
            int rightScore,
            int rightWeight
    ) {
        int totalWeight = leftWeight + rightWeight;
        if (totalWeight == 0) {
            return 0;
        }

        double weightedSum = (leftScore * leftWeight) + (rightScore * rightWeight);
        return (int) Math.round(weightedSum / totalWeight);
    }

    private int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
