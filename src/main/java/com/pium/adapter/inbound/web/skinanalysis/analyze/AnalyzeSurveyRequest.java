package com.pium.adapter.inbound.web.skinanalysis.analyze;

import com.pium.application.skinanalysis.analyze.dto.AnalyzeCommand;

import java.util.List;

/**
 * 설문 분석 요청 DTO
 */
public record AnalyzeSurveyRequest(
        List<AnswerRequest> answers
) {

    public AnalyzeCommand toCommand() {
        List<AnalyzeCommand.Answer> mapped = answers.stream()
                .map(a -> new AnalyzeCommand.Answer(a.questionId(), a.selectedOptionCodes()))
                .toList();
        return new AnalyzeCommand(mapped);
    }

    /**
     * 문항 응답 요청 모델
     */
    public record AnswerRequest(
            String questionId,
            List<String> selectedOptionCodes
    ) {
    }
}
