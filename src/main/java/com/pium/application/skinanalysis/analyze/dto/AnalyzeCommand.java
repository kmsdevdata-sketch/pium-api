package com.pium.application.skinanalysis.analyze.dto;

import java.util.List;

/**
 * 유즈케이스 입력 커맨드
 *
 * @param answers 문항 응답 목록
 * @param goals 사용자가 선택한 고민목록
 */
public record AnalyzeCommand(
        List<Answer> answers,
        List<String> goals
) {

    /**
     * 문항 응답 모델
     *
     * 단일 선택/복수 선택을 모두 수용하기 위해 selectedOptionCodes를 리스트로 둔다
     * - 단일 선택 : 리스트 크기 1
     * - 복수 선택 : 리스트 크기 N
     *
     * @param questionId 설문 문항 식별자 (ex.Q_DRYNESS_1)
     * @param selectedOptionCodes 사용자가 선택한 옵션 코드 목록 (ex.Q1_1)
     */
    public record Answer(
            String questionId,
            List<String> selectedOptionCodes
    ) {
    }

}
