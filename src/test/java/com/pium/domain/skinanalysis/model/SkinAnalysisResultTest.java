package com.pium.domain.skinanalysis.model;

import com.pium.domain.skinanalysis.enumtype.IngredientGroup;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import com.pium.domain.skinanalysis.exception.SkinAnalysisException;
import com.pium.domain.skinanalysis.fixture.SkinAnalysisResultFixture;
import com.pium.domain.skinanalysis.vo.RequiredIngredient;
import com.pium.domain.skinanalysis.vo.RulesVersion;
import com.pium.domain.skinanalysis.vo.SkinAnalysisResultId;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkinAnalysisResultTest {

    UserId userId;
    RulesVersion rulesVersion;
    List<SkinMetricScore> skinMetricScores;
    List<RequiredIngredient> requiredIngredients;

    @BeforeEach
    void setUp() {
        userId = SkinAnalysisResultFixture.createUserId();
        rulesVersion = SkinAnalysisResultFixture.createRulesVersion();
        skinMetricScores = SkinAnalysisResultFixture.createSkinMetricScores();
        requiredIngredients = SkinAnalysisResultFixture.createRequiredIngredients();
    }

    @Test
    void 피부분석결과_생성_검증() {
        SkinAnalysisResult result = SkinAnalysisResult.create(
                userId,
                rulesVersion,
                skinMetricScores,
                requiredIngredients
        );

        assertThat(result.getId()).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getRulesVersion()).isEqualTo(rulesVersion);
        assertThat(result.getSkinMetricScores()).isEqualTo(skinMetricScores);
        assertThat(result.getRequiredIngredients()).isEqualTo(requiredIngredients);
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
                rulesVersion,
                skinMetricScores,
                requiredIngredients,
                createdAt,
                updatedAt
        );

        assertThat(reconstituted.getId()).isEqualTo(id);
        assertThat(reconstituted.getUserId()).isEqualTo(userId);
        assertThat(reconstituted.getRulesVersion()).isEqualTo(rulesVersion);
        assertThat(reconstituted.getSkinMetricScores()).isEqualTo(skinMetricScores);
        assertThat(reconstituted.getRequiredIngredients()).isEqualTo(requiredIngredients);
        assertThat(reconstituted.getCreatedAt()).isEqualTo(createdAt);
        assertThat(reconstituted.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void 피부상태점수목록_비어있으면_예외_검증() {
        assertThatThrownBy(() -> SkinAnalysisResult.create(
                userId,
                rulesVersion,
                List.of(),
                requiredIngredients
        )).isInstanceOf(SkinAnalysisException.class);
    }

    @Test
    void 필요성분군목록_비어있으면_예외_검증() {
        assertThatThrownBy(() -> SkinAnalysisResult.create(
                userId,
                rulesVersion,
                skinMetricScores,
                List.of()
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
    void 필요성분군가중치_경계값_검증() {
        RequiredIngredient min = RequiredIngredient.of(IngredientGroup.HYDRATION, 0);
        RequiredIngredient max = RequiredIngredient.of(IngredientGroup.HYDRATION, 100);

        assertThat(min.weight()).isEqualTo(0);
        assertThat(max.weight()).isEqualTo(100);
    }

    @Test
    void 필요성분군가중치_범위초과시_예외_검증() {
        assertThatThrownBy(() -> RequiredIngredient.of(IngredientGroup.HYDRATION, -1))
                .isInstanceOf(SkinAnalysisException.class);

        assertThatThrownBy(() -> RequiredIngredient.of(IngredientGroup.HYDRATION, 101))
                .isInstanceOf(SkinAnalysisException.class);
    }

    @Test
    void 룰버전_blank_예외_검증() {
        assertThatThrownBy(() -> RulesVersion.of(" "))
                .isInstanceOf(SkinAnalysisException.class);
    }
}
