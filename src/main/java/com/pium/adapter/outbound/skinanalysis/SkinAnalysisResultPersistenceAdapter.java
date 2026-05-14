package com.pium.adapter.outbound.skinanalysis;

import com.pium.adapter.outbound.skinanalysis.persistence.entity.SkinAnalysisResultEntity;
import com.pium.adapter.outbound.skinanalysis.persistence.mapper.PersistenceBundle;
import com.pium.adapter.outbound.skinanalysis.persistence.repository.SkinAnalysisResultJpaRepository;
import com.pium.adapter.outbound.skinanalysis.persistence.repository.SkinMetricScoreJpaRepository;
import com.pium.application.skinanalysis.analyze.required.SaveSkinAnalysisResultPort;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbound Adapter
 * - SkinAnalysisResult aggregate를 영속 모델로 변환해 저장한다
 * - 루트(result) 저장후 상태 점수 자식(score) 목록을 같은 트랜잭션에 저장한다
 */

@Component
@Transactional
@RequiredArgsConstructor
public class SkinAnalysisResultPersistenceAdapter implements SaveSkinAnalysisResultPort {

    private final SkinAnalysisResultJpaRepository skinAnalysisResultJpaRepository;
    private final SkinMetricScoreJpaRepository skinMetricScoreJpaRepository;

    @Override
    public void save(SkinAnalysisResult result) {

        PersistenceBundle bundle = SkinAnalysisResultEntity.from(result);
        skinAnalysisResultJpaRepository.save(bundle.entity());
        skinMetricScoreJpaRepository.saveAll(bundle.scoreEntities());
    }
}
