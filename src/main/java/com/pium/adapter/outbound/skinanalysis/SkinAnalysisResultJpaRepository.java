package com.pium.adapter.outbound.skinanalysis;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SkinAnalysis 결과 저장소
 */
public interface SkinAnalysisResultJpaRepository extends JpaRepository<SkinAnalysisResultEntity, String> {

}
