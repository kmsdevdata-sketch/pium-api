package com.pium.adapter.outbound.skinanalysis;

import com.pium.adapter.outbound.skinanalysis.fixture.SkinAnalysisResultFixture;
import com.pium.adapter.outbound.skinanalysis.persistence.repository.SkinAnalysisResultJpaRepository;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(SkinAnalysisResultPersistenceAdapter.class)
class SkinAnalysisResultPersistenceAdapterTest {

    @Autowired
    SkinAnalysisResultJpaRepository skinAnalysisResultJpaRepository;

    @Autowired
    private SkinAnalysisResultPersistenceAdapter persistenceAdapter;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void save_루트_점수_goals_저장검증() {
        SkinAnalysisResult result = SkinAnalysisResult.create(
                SkinAnalysisResultFixture.createUserId(),
                SkinAnalysisResultFixture.createSkinMetricScores(),
                SkinAnalysisResultFixture.createGoals()
        );

        persistenceAdapter.save(result);

        entityManager.flush();
        entityManager.clear();

        var savedRoot = skinAnalysisResultJpaRepository.findById(result.getId().value()).orElseThrow();

        assertThat(savedRoot.getResultId()).isEqualTo(result.getId().value());
        assertThat(savedRoot.getUserId()).isEqualTo(result.getUserId().value());
        assertThat(savedRoot.getGoals()).containsExactlyElementsOf(result.getGoals());

        Long scoreCount = entityManager.createQuery(
                        "select count(s) from SkinMetricScoreEntity s where s.result.resultId = :resultId",
                        Long.class
                ).setParameter("resultId", result.getId().value())
                .getSingleResult();

        assertThat(scoreCount).isEqualTo((long) result.getSkinMetricScores().size());
    }
}
