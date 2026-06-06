package com.pium.adapter.inbound.web.skinanalysis.image;

import com.pium.application.skinanalysis.image.dto.PreAnalyzeImageView;
import com.pium.application.skinanalysis.spec.dto.SurveySpecView;

import java.util.List;

public record PreAnalyzeSkinImageResponse(
        String analysisSessionId,
        List<QuestionResponse> questions,
        QuestionResponse goalQuestion
) {

    public static PreAnalyzeSkinImageResponse from(PreAnalyzeImageView view) {
        return new PreAnalyzeSkinImageResponse(
                view.analysisSessionId(),
                view.questions().stream()
                        .map(PreAnalyzeSkinImageResponse::question)
                        .toList(),
                question(view.goalQuestion())
        );
    }

    private static QuestionResponse question(SurveySpecView.Question question) {
        return new QuestionResponse(
                question.questionId(),
                question.title(),
                question.options().stream()
                        .map(option -> new OptionResponse(option.code(), option.label()))
                        .toList()
        );
    }

    public record QuestionResponse(
            String questionId,
            String title,
            List<OptionResponse> options
    ) {
    }

    public record OptionResponse(
            String code,
            String label
    ) {
    }
}
