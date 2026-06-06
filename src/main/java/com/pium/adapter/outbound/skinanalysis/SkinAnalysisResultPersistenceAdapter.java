package com.pium.adapter.outbound.skinanalysis;

import com.pium.adapter.outbound.skinanalysis.persistence.entity.SkinAnalysisResultEntity;
import com.pium.adapter.outbound.skinanalysis.persistence.entity.SkinMetricScoreEntity;
import com.pium.adapter.outbound.skinanalysis.persistence.mapper.PersistenceBundle;
import com.pium.adapter.outbound.skinanalysis.persistence.repository.SkinAnalysisResultJpaRepository;
import com.pium.adapter.outbound.skinanalysis.persistence.repository.SkinMetricScoreJpaRepository;
import com.pium.application.skinanalysis.analyze.required.SaveSkinAnalysisResultPort;
import com.pium.application.skinanalysis.result.required.LoadSkinAnalysisResultPort;
import com.pium.application.user.bootstrap.required.CheckUserDiagnosisPort;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import com.pium.domain.skinanalysis.vo.SkinAnalysisResultId;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;
import com.pium.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Outbound Adapter
 * - SkinAnalysisResult aggregate를 영속 모델로 변환해 저장한다
 * - 루트(result) 저장후 상태 점수 자식(score) 목록을 같은 트랜잭션에 저장한다
 */

@Component
@Transactional
@RequiredArgsConstructor
public class SkinAnalysisResultPersistenceAdapter implements SaveSkinAnalysisResultPort, CheckUserDiagnosisPort, LoadSkinAnalysisResultPort {

    private final SkinAnalysisResultJpaRepository skinAnalysisResultJpaRepository;
    private final SkinMetricScoreJpaRepository skinMetricScoreJpaRepository;

    @Override
    public void save(SkinAnalysisResult result) {

        PersistenceBundle bundle = SkinAnalysisResultEntity.from(result);
        skinAnalysisResultJpaRepository.save(bundle.entity());
        skinMetricScoreJpaRepository.saveAll(bundle.scoreEntities());
    }

    @Override
    public boolean existsByUserId(UserId userId) {
        return skinAnalysisResultJpaRepository.existsByUserId(userId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserId(UserId userId) {
        return skinAnalysisResultJpaRepository.countByUserId(userId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkinAnalysisResult> loadAll(UserId userId) {
        return skinAnalysisResultJpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SkinAnalysisResult> loadLatest(UserId userId) {
        return skinAnalysisResultJpaRepository.findTopByUserIdOrderByCreatedAtDesc(userId.value())
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SkinAnalysisResult> load(UserId userId, SkinAnalysisResultId resultId) {
        return skinAnalysisResultJpaRepository.findByResultIdAndUserId(resultId.value(), userId.value())
                .map(this::toDomain);
    }

    private SkinAnalysisResult toDomain(SkinAnalysisResultEntity entity) {
        List<SkinMetricScore> scores = skinMetricScoreJpaRepository.findAllByResultResultIdOrderByIdAsc(entity.getResultId()).stream()
                .map(this::toDomainScore)
                .toList();

        return SkinAnalysisResult.reconstitute(
                SkinAnalysisResultId.of(entity.getResultId()),
                UserId.of(entity.getUserId()),
                scores,
                entity.getGoals(),
                entity.getAnalysisType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private SkinMetricScore toDomainScore(SkinMetricScoreEntity entity) {
        return SkinMetricScore.of(entity.getMetric(), entity.getScore());
    }
}
