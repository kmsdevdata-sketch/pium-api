package com.pium.domain.skinanalysis.model;

import com.pium.domain.skinanalysis.enumtype.IngredientGroup;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import com.pium.domain.skinanalysis.exception.SkinAnalysisException;
import com.pium.domain.skinanalysis.fixture.SkinAnalysisResultFixture;
import com.pium.domain.skinanalysis.vo.SkinAnalysisResultId;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkinAnalysisResultTest {

    UserId userId;
    List<SkinMetricScore> skinMetricScores;
    List<String> goals;

    @BeforeEach
    void setUp() {
        userId = SkinAnalysisResultFixture.createUserId();
        skinMetricScores = SkinAnalysisResultFixture.createSkinMetricScores();
        goals = SkinAnalysisResultFixture.createGoals();
    }

    @Test
    void 피부분석결과_생성_검증() {
        SkinAnalysisResult result = SkinAnalysisResult.create(
                userId,
                skinMetricScores,
                goals
        );

        assertThat(result.getId()).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getSkinMetricScores()).isEqualTo(skinMetricScores);
        assertThat(result.getCreatedAt()).isEqualTo(result.getUpdatedAt());
    }

    @Test
    void 피부분석결과_복원_검증() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 9, 10, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 5, 9, 11, 0, 0);

        SkinAnalysisResultId id = SkinAnalysisResultId.newId();
        SkinAnalysisResult reconstituted = SkinAnalysisResult.reconstitute(
                id,
                userId,
                skinMetricScores,
                goals,
                createdAt,
                updatedAt
        );

        assertThat(reconstituted.getId()).isEqualTo(id);
        assertThat(reconstituted.getUserId()).isEqualTo(userId);
        assertThat(reconstituted.getSkinMetricScores()).isEqualTo(skinMetricScores);
        assertThat(reconstituted.getCreatedAt()).isEqualTo(createdAt);
        assertThat(reconstituted.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void 피부상태점수목록_비어있으면_예외_검증() {
        assertThatThrownBy(() -> SkinAnalysisResult.create(
                userId,
                List.of(),
                goals
        )).isInstanceOf(SkinAnalysisException.class);
    }

    @Test
    void 피부상태점수_경계값_검증() {
        SkinMetricScore min = SkinMetricScore.of(SkinMetric.OILINESS, 0);
        SkinMetricScore max = SkinMetricScore.of(SkinMetric.OILINESS, 100);

        assertThat(min.score()).isEqualTo(0);
        assertThat(max.score()).isEqualTo(100);
    }

    @Test
    void 피부상태점수_범위초과시_예외_검증() {
        assertThatThrownBy(() -> SkinMetricScore.of(SkinMetric.OILINESS, -1))
                .isInstanceOf(SkinAnalysisException.class);

        assertThatThrownBy(() -> SkinMetricScore.of(SkinMetric.OILINESS, 101))
                .isInstanceOf(SkinAnalysisException.class);
    }

    @Test
    void 목표목록_null이면_예외_검증() {
        assertThatThrownBy(() -> SkinAnalysisResult.create(
                userId,
                skinMetricScores,
                null
        )).isInstanceOf(SkinAnalysisException.class);
    }

    @Test
    void 목표목록_비어있으면_예외_검증() {
        assertThatThrownBy(() -> SkinAnalysisResult.create(
                userId,
                skinMetricScores,
                List.of()
        )).isInstanceOf(SkinAnalysisException.class);
    }

    @Test
    void goals_불변_검증() {
        SkinAnalysisResult result = SkinAnalysisResult.create(userId, skinMetricScores, goals);

        assertThatThrownBy(() -> result.getGoals().add("Q11_3"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void 생성후_원본리스트_변경해도_결과불변_검증() {
        List<String> mutableGoals = new ArrayList<>(goals);

        SkinAnalysisResult result = SkinAnalysisResult.create(userId, skinMetricScores, mutableGoals);
        mutableGoals.add("Q11_5");

        assertThat(result.getGoals()).doesNotContain("Q11_5");
    }

}
