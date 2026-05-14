package com.pium.adapter.outbound.skinanalysis.persistence.mapper;

import com.pium.adapter.outbound.skinanalysis.persistence.entity.SkinAnalysisResultEntity;
import com.pium.adapter.outbound.skinanalysis.persistence.entity.SkinMetricScoreEntity;

import java.util.List;

/**
 * SkinAnalysisResult 도메인을 영속화 단위로 분해한 묶음.
 * - entity: 결과 루트
 * - scoreEntities: 상태 점수 자식 목록
 */
public record PersistenceBundle(
        SkinAnalysisResultEntity entity,
        List<SkinMetricScoreEntity> scoreEntities
) {
}
