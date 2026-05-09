package com.layerd.domain.skinanalysis.model;

import com.layerd.domain.skinanalysis.exception.SkinAnalysisErrorCode;
import com.layerd.domain.skinanalysis.exception.SkinAnalysisException;
import com.layerd.domain.skinanalysis.vo.RequiredIngredient;
import com.layerd.domain.skinanalysis.vo.RulesVersion;
import com.layerd.domain.skinanalysis.vo.SkinAnalysisResultId;
import com.layerd.domain.skinanalysis.vo.SkinMetricScore;
import com.layerd.domain.user.vo.UserId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SkinAnalysisResult {

    private final SkinAnalysisResultId id;
    private final UserId userId;
    private final RulesVersion rulesVersion;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final List<SkinMetricScore> skinMetricScores;
    private final List<RequiredIngredient> requiredIngredients;

    private SkinAnalysisResult(
            SkinAnalysisResultId id,
            UserId userId,
            RulesVersion rulesVersion,
            List<SkinMetricScore> skinMetricScores,
            List<RequiredIngredient> requiredIngredients,
            LocalDateTime createdAt
    ) {
        validateScores(skinMetricScores);
        validateIngredients(requiredIngredients);

        this.id = id;
        this.userId = userId;
        this.rulesVersion = rulesVersion;
        this.skinMetricScores = List.copyOf(skinMetricScores);
        this.requiredIngredients = List.copyOf(requiredIngredients);
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static SkinAnalysisResult create(
            UserId userId,
            RulesVersion rulesVersion,
            List<SkinMetricScore> skinMetricScores,
            List<RequiredIngredient> requiredIngredients
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new SkinAnalysisResult(
                SkinAnalysisResultId.newId(),
                userId,
                rulesVersion,
                skinMetricScores,
                requiredIngredients,
                now
        );
    }

    public static SkinAnalysisResult reconstitute(
            SkinAnalysisResultId id,
            UserId userId,
            RulesVersion rulesVersion,
            List<SkinMetricScore> skinMetricScores,
            List<RequiredIngredient> requiredIngredients,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        SkinAnalysisResult result = new SkinAnalysisResult(
                id,
                userId,
                rulesVersion,
                skinMetricScores,
                requiredIngredients,
                createdAt
        );
        result.updatedAt = updatedAt;
        return result;
    }

    private static void validateScores(List<SkinMetricScore> scores) {
        if (scores == null || scores.isEmpty()) {
            throw new SkinAnalysisException(SkinAnalysisErrorCode.SKIN_METRIC_SCORES_EMPTY);
        }
    }

    private static void validateIngredients(List<RequiredIngredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new SkinAnalysisException(SkinAnalysisErrorCode.REQUIRED_INGREDIENTS_EMPTY);
        }
    }
}
