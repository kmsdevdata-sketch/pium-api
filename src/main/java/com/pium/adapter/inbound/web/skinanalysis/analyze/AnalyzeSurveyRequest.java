package com.pium.adapter.inbound.web.skinanalysis.analyze;

import com.pium.application.skinanalysis.analyze.dto.AnalyzeCommand;
import com.pium.domain.user.vo.UserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 설문 분석 요청 DTO
 */
public record AnalyzeSurveyRequest(
        @NotEmpty
        List<@Valid @NotNull AnswerRequest> answers,

        @NotEmpty
        List<@NotBlank String> goals
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
            @NotBlank
            String questionId,

            @NotEmpty
            List<@NotBlank String> selectedOptionCodes
    ) {
    }
}
