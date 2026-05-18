package com.pium.adapter.outbound.skinanalysis.persistence.repository;

import com.pium.adapter.outbound.skinanalysis.persistence.entity.SkinAnalysisResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * SkinAnalysis 결과 저장소
 */
public interface SkinAnalysisResultJpaRepository extends JpaRepository<SkinAnalysisResultEntity, String> {

    long countByUserId(String userId);

    boolean existsByUserId(String userId);

    List<SkinAnalysisResultEntity> findAllByUserIdOrderByCreatedAtDesc(String userId);

    Optional<SkinAnalysisResultEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);

    Optional<SkinAnalysisResultEntity> findByResultIdAndUserId(String resultId, String userId);
}
