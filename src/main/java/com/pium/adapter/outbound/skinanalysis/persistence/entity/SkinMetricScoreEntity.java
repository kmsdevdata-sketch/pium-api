package com.pium.adapter.outbound.skinanalysis.persistence.entity;

import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "skin_metric_score")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkinMetricScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "result_id", nullable = false)
    private SkinAnalysisResultEntity result;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric", nullable = false, length = 64)
    private SkinMetric metric;

    @Column(name = "score_value", nullable = false)
    private int score;

    private SkinMetricScoreEntity(
            SkinAnalysisResultEntity result,
            SkinMetric metric,
            int score
    ) {
        this.result = result;
        this.metric = metric;
        this.score = score;
    }

    public static SkinMetricScoreEntity of(
            SkinAnalysisResultEntity result,
            SkinMetric metric,
            int score
    ) {
        return new SkinMetricScoreEntity(result, metric, score);
    }
}
