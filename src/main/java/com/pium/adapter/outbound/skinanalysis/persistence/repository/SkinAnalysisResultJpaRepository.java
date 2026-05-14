package com.pium.adapter.outbound.skinanalysis.persistence.repository;

import com.pium.adapter.outbound.skinanalysis.persistence.entity.SkinAnalysisResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SkinAnalysis 결과 저장소
 */
public interface SkinAnalysisResultJpaRepository extends JpaRepository<SkinAnalysisResultEntity, String> {

}
