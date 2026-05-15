package com.pium.application.skinanalysis.analyze.required.dto;

import java.util.List;

/**
 * 정규화된 설문 제출 모델
 * - 엔진이 UI응답 형식을 몰라도 되도록 내부 표준 계약으로 분리
 */
public record NormalizeSurveySubmission(
        List<NormalizedAnswer> answers
) {


    /**
     * 정규화된 문답 응답
     *
     * @param questionId 문항 식별자
     * @param selectedOptionCodes 선택 옵션 코드 목록
     */
    public record NormalizedAnswer(
            String questionId,
            List<String> selectedOptionCodes
    ) {
    }
}
