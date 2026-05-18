package com.pium.domain.skinanalysis.model;

import com.pium.domain.skinanalysis.exception.SkinAnalysisErrorCode;
import com.pium.domain.skinanalysis.exception.SkinAnalysisException;
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
    private final List<String> goals;

    private SkinAnalysisResult(
            SkinAnalysisResultId id,
            UserId userId,
            List<SkinMetricScore> skinMetricScores,
            List<String> goals,
            LocalDateTime createdAt
    ) {
        validateScores(skinMetricScores);
        validateGoals(goals);

        this.id = id;
        this.userId = userId;
        this.skinMetricScores = List.copyOf(skinMetricScores);
        this.goals = List.copyOf(goals);
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static SkinAnalysisResult create(
            UserId userId,
            List<SkinMetricScore> skinMetricScores,
            List<String> goals
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new SkinAnalysisResult(
                SkinAnalysisResultId.newId(),
                userId,
                skinMetricScores,
                goals,
                now
        );
    }

    public static SkinAnalysisResult reconstitute(
            SkinAnalysisResultId id,
            UserId userId,
            List<SkinMetricScore> skinMetricScores,
            List<String> goals,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        SkinAnalysisResult result = new SkinAnalysisResult(
                id,
                userId,
                skinMetricScores,
                goals,
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

    private static void validateGoals(List<String> goals) {
        if (goals == null || goals.isEmpty()) {
            throw new SkinAnalysisException(SkinAnalysisErrorCode.SKIN_GOALS_REQUIRED);
        }
    }

}
