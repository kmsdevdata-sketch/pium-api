package com.pium.adapter.outbound.skinanalysis;

import com.pium.adapter.outbound.skinanalysis.fixture.SkinAnalysisResultFixture;
import com.pium.adapter.outbound.skinanalysis.persistence.repository.SkinAnalysisResultJpaRepository;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import com.pium.domain.skinanalysis.vo.SkinAnalysisResultId;
import com.pium.domain.user.vo.UserId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

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

    @Test
    void existsByUserId_저장된_진단이력이_있으면_true를_반환한다() {
        SkinAnalysisResult result = SkinAnalysisResult.create(
                SkinAnalysisResultFixture.createUserId(),
                SkinAnalysisResultFixture.createSkinMetricScores(),
                SkinAnalysisResultFixture.createGoals()
        );
        persistenceAdapter.save(result);

        entityManager.flush();
        entityManager.clear();

        assertThat(persistenceAdapter.existsByUserId(result.getUserId())).isTrue();
        assertThat(persistenceAdapter.existsByUserId(UserId.of("user-test-999"))).isFalse();
    }

    @Test
    void loadLatest_사용자의_최신_결과를_조회한다() {
        UserId userId = SkinAnalysisResultFixture.createUserId();
        SkinAnalysisResult older = SkinAnalysisResult.reconstitute(
                SkinAnalysisResultId.of("result-older"),
                userId,
                SkinAnalysisResultFixture.createSkinMetricScores(),
                SkinAnalysisResultFixture.createGoals(),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(2)
        );
        SkinAnalysisResult latest = SkinAnalysisResult.reconstitute(
                SkinAnalysisResultId.of("result-latest"),
                userId,
                SkinAnalysisResultFixture.createSkinMetricScores(),
                SkinAnalysisResultFixture.createGoals(),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );

        persistenceAdapter.save(older);
        persistenceAdapter.save(latest);

        entityManager.flush();
        entityManager.clear();

        SkinAnalysisResult loaded = persistenceAdapter.loadLatest(userId).orElseThrow();

        assertThat(loaded.getId()).isEqualTo(latest.getId());
        assertThat(loaded.getSkinMetricScores()).hasSize(7);
    }

    @Test
    void loadAll_사용자의_결과목록을_최신순으로_조회한다() {
        UserId userId = SkinAnalysisResultFixture.createUserId();
        SkinAnalysisResult older = SkinAnalysisResult.reconstitute(
                SkinAnalysisResultId.of("result-list-older"),
                userId,
                SkinAnalysisResultFixture.createSkinMetricScores(),
                SkinAnalysisResultFixture.createGoals(),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(2)
        );
        SkinAnalysisResult latest = SkinAnalysisResult.reconstitute(
                SkinAnalysisResultId.of("result-list-latest"),
                userId,
                SkinAnalysisResultFixture.createSkinMetricScores(),
                SkinAnalysisResultFixture.createGoals(),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );

        persistenceAdapter.save(older);
        persistenceAdapter.save(latest);

        entityManager.flush();
        entityManager.clear();

        assertThat(persistenceAdapter.loadAll(userId))
                .extracting(result -> result.getId().value())
                .containsExactly("result-list-latest", "result-list-older");
    }

    @Test
    void load_특정_결과를_조회한다() {
        SkinAnalysisResult result = SkinAnalysisResult.create(
                SkinAnalysisResultFixture.createUserId(),
                SkinAnalysisResultFixture.createSkinMetricScores(),
                SkinAnalysisResultFixture.createGoals()
        );
        persistenceAdapter.save(result);

        entityManager.flush();
        entityManager.clear();

        SkinAnalysisResult loaded = persistenceAdapter.load(result.getUserId(), result.getId()).orElseThrow();

        assertThat(loaded.getId()).isEqualTo(result.getId());
        assertThat(loaded.getGoals()).containsExactlyElementsOf(result.getGoals());
        assertThat(persistenceAdapter.load(UserId.of("user-test-999"), result.getId())).isEmpty();
    }
}
