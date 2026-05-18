package com.pium.application.skinanalysis.analyze.required.dto;

import com.pium.domain.skinanalysis.vo.SkinMetricScore;

import java.util.List;

public record AnalyzedSkinMetrics(
        List<SkinMetricScore> skinMetricScores
) {
}
