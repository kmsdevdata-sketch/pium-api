package com.pium.adapter.outbound.skinanalysis;

import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SkinAnalysis 결과 영속 엔티티
 * - 도메인 객체와 분리된 JPA 전용 모델
 */

@Entity
@Table(name = "skin_analysis_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkinAnalysisResultEntity {

    @Id
    // 도메인 식별자(VO) 원문을 PK로 사용
    @Column(name = "result_id", nullable = false, length = 64)
    private String resultId;

    // UserId VO 원문
    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private SkinAnalysisResultEntity(
            String resultId,
            String userId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.resultId = resultId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PersistenceBundle from(SkinAnalysisResult domain) {
        SkinAnalysisResultEntity entity = new SkinAnalysisResultEntity(
                domain.getId().value(),
                domain.getUserId().value(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );

        List<SkinMetricScoreEntity> scoreEntities = domain.getSkinMetricScores().stream()
                .map(s -> SkinMetricScoreEntity.of(entity, s.metric(), s.score()))
                .toList();

        return new PersistenceBundle(entity, scoreEntities);
    }

}
