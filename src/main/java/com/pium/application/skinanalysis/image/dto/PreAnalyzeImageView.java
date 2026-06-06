package com.pium.application.skinanalysis.image.dto;

import com.pium.application.skinanalysis.spec.dto.SurveySpecView;

import java.util.List;

public record PreAnalyzeImageView(
        String analysisSessionId,
        List<SurveySpecView.Question> questions,
        SurveySpecView.Question goalQuestion
) {
}
