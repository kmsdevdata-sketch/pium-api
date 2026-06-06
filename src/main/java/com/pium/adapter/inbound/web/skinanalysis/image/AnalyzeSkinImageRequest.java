package com.pium.adapter.inbound.web.skinanalysis.image;

import com.pium.application.skinanalysis.image.dto.AnalyzeImageCommand;
import com.pium.domain.user.vo.UserId;

import java.util.List;

/**
 * 사진 기반 최종 분석 요청 DTO
 *
 * 선분석 세션과 보조 문항을 내부 command로 변환한다.
 */
public record AnalyzeSkinImageRequest(
    String analysisSessionId,
    List<AnswerRequest> answers,
    List<String> goals
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
            String questionId,
            List<String> selectedOptionCodes
    ) {
    }

}
