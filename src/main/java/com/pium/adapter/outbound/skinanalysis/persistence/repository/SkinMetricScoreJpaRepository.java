package com.pium.adapter.outbound.skinanalysis.persistence.repository;

import com.pium.adapter.outbound.skinanalysis.persistence.entity.SkinMetricScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * SkinMetric 결과 저장소
 */
public interface SkinMetricScoreJpaRepository extends JpaRepository<SkinMetricScoreEntity,Long> {

    List<SkinMetricScoreEntity> findAllByResultResultIdOrderByIdAsc(String resultId);
}
