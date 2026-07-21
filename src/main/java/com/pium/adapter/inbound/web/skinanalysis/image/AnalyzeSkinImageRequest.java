package com.pium.adapter.inbound.web.skinanalysis.image;

import com.pium.application.skinanalysis.image.dto.AnalyzeImageCommand;
import com.pium.domain.user.vo.UserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 사진 기반 최종 분석 요청 DTO
 *
 * 선분석 세션과 보조 문항을 내부 command로 변환한다.
 */
public record AnalyzeSkinImageRequest(
    @NotBlank
    String analysisSessionId,

    @NotEmpty
    List<@Valid @NotNull AnswerRequest> answers,

    @NotEmpty
    List<@NotBlank String> goals
) {

    public AnalyzeSkinImageRequest {
        answers = answers == null ? List.of() : List.copyOf(answers);
        goals = goals == null ? List.of() : List.copyOf(goals);
    }

    public AnalyzeImageCommand toCommand(UserId userId) {
        List<AnalyzeImageCommand.Answer> mapped = answers.stream()
                .map(answer -> new AnalyzeImageCommand.Answer(
                        answer.questionId(),
                        answer.selectedOptionCodes()
                ))
                .toList();

        return new AnalyzeImageCommand(
                userId,
                analysisSessionId,
                mapped,
                goals
        );
    }

    public record AnswerRequest(
            @NotBlank
            String questionId,

            @NotEmpty
            List<@NotBlank String> selectedOptionCodes
    ) {
    }

}
