package com.pium.adapter.inbound.web.skinanalysis.analyze;

import com.pium.application.skinanalysis.analyze.dto.AnalyzeCommand;
import com.pium.domain.user.vo.UserId;

import java.util.List;

/**
 * 설문 분석 요청 DTO
 */
public record AnalyzeSurveyRequest(
        List<AnswerRequest> answers,
        List<String> goals
) {

    public AnalyzeCommand toCommand(UserId userId) {
        List<AnalyzeCommand.Answer> mapped = answers.stream()
                .map(a -> new AnalyzeCommand.Answer(a.questionId(), a.selectedOptionCodes()))
                .toList();
        return new AnalyzeCommand(userId, mapped, goals);
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
