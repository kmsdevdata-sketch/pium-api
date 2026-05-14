package com.pium.domain.skinanalysis.model;

import com.pium.domain.skinanalysis.exception.SkinAnalysisErrorCode;
import com.pium.domain.skinanalysis.exception.SkinAnalysisException;
import com.pium.domain.skinanalysis.vo.RequiredIngredient;
import com.pium.domain.skinanalysis.vo.RulesVersion;
import com.pium.domain.skinanalysis.vo.SkinAnalysisResultId;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;
import com.pium.domain.user.vo.UserId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SkinAnalysisResult {

    private final SkinAnalysisResultId id;
    private final UserId userId;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final List<SkinMetricScore> skinMetricScores;

    private SkinAnalysisResult(
            SkinAnalysisResultId id,
            UserId userId,
            List<SkinMetricScore> skinMetricScores,
            LocalDateTime createdAt
    ) {
        validateScores(skinMetricScores);

        this.id = id;
        this.userId = userId;
        this.skinMetricScores = List.copyOf(skinMetricScores);
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static SkinAnalysisResult create(
            UserId userId,
            List<SkinMetricScore> skinMetricScores
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new SkinAnalysisResult(
                SkinAnalysisResultId.newId(),
                userId,
                skinMetricScores,
                now
        );
    }

    public static SkinAnalysisResult reconstitute(
            SkinAnalysisResultId id,
            UserId userId,
            List<SkinMetricScore> skinMetricScores,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        SkinAnalysisResult result = new SkinAnalysisResult(
                id,
                userId,
                skinMetricScores,
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


}
