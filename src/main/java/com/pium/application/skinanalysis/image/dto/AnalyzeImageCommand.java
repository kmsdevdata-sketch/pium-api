package com.pium.application.skinanalysis.image.dto;

import com.pium.domain.user.vo.UserId;

import java.util.List;

/**
 * 사진 선분석 세션과 보조 문항을 종합하는 유즈케이스 입력.
 */
public record AnalyzeImageCommand(
        UserId userId,
        String analysisSessionId,
        List<Answer> answers,
        List<String> goals
) {

    public AnalyzeImageCommand {
        answers = answers == null ? List.of() : List.copyOf(answers);
        goals = goals == null ? List.of() : List.copyOf(goals);
    }

    public record Answer(
            String questionId,
            List<String> selectedOptionCodes
    ) {

        public Answer {
            selectedOptionCodes = selectedOptionCodes == null ? List.of() : List.copyOf(selectedOptionCodes);
        }
    }
}
