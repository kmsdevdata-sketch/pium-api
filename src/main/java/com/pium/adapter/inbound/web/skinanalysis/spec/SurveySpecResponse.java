package com.pium.adapter.inbound.web.skinanalysis.spec;

import com.pium.application.skinanalysis.spec.dto.SurveySpecView;

import java.util.List;

public record SurveySpecResponse(List<QuestionResponse> questionResponses) {

    public static SurveySpecResponse from(SurveySpecView surveySpecView) {
        return new SurveySpecResponse(
                surveySpecView.questions().stream()
                        .map(question -> new QuestionResponse(
                                question.questionId(),
                                question.title(),
                                question.options().stream()
                                        .map(option -> new OptionResponse(
                                                option.code(),
                                                option.label()
                                        )).toList()
                        )).toList()
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

