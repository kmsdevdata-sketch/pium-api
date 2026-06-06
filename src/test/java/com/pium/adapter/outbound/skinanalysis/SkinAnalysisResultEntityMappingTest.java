package com.pium.adapter.outbound.skinanalysis;

import com.pium.adapter.outbound.skinanalysis.fixture.SkinAnalysisResultFixture;
import com.pium.adapter.outbound.skinanalysis.persistence.entity.SkinAnalysisResultEntity;
import com.pium.adapter.outbound.skinanalysis.persistence.mapper.PersistenceBundle;
import com.pium.domain.skinanalysis.enumtype.SkinAnalysisType;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SkinAnalysisResultEntityMappingTest {

    SkinAnalysisResult skinAnalysisResult;

    @BeforeEach
    void setUp() {
        skinAnalysisResult = SkinAnalysisResultFixture.createSkinAnalysisResult();
    }

    @Test
    void from_호출시_bundle_정상반환() {
        PersistenceBundle bundle = SkinAnalysisResultEntity.from(skinAnalysisResult);

        assertThat(bundle).isNotNull();
        assertThat(bundle.entity()).isNotNull();
        assertThat(bundle.scoreEntities()).isNotNull();

        // root 매핑 검증
        assertThat(bundle.entity().getResultId()).isEqualTo(skinAnalysisResult.getId().value());
        assertThat(bundle.entity().getUserId()).isEqualTo(skinAnalysisResult.getUserId().value());
        assertThat(bundle.entity().getAnalysisType()).isEqualTo(SkinAnalysisType.SURVEY);
        assertThat(bundle.entity().getGoals()).containsExactlyElementsOf(skinAnalysisResult.getGoals());
        assertThat(bundle.entity().getCreatedAt()).isEqualTo(skinAnalysisResult.getCreatedAt());
        assertThat(bundle.entity().getUpdatedAt()).isEqualTo(skinAnalysisResult.getUpdatedAt());

        // score 목록 매핑 검증
        assertThat(bundle.scoreEntities()).hasSize(skinAnalysisResult.getSkinMetricScores().size());

        for (int i = 0; i < bundle.scoreEntities().size(); i++) {
            var scoreEntity = bundle.scoreEntities().get(i);
            var domainScore = skinAnalysisResult.getSkinMetricScores().get(i);

            assertThat(scoreEntity.getResult()).isSameAs(bundle.entity());
            assertThat(scoreEntity.getMetric()).isEqualTo(domainScore.metric());
            assertThat(scoreEntity.getScore()).isEqualTo(domainScore.score());
        }
    }
}
