package com.pium.application.skinanalysis.survey.provided.dto;
import java.util.List;

/**
 * 설문 스펙 응답 DTO
 * - 항상 최신 스펙만 내려준다고 가정
 * - version은 추적용 메타로만 유지
 */

/**
 * 설문 스펙 조회 응답 DTO (최소형)
 */
public record SurveySpecView(
        List<Question> questions
) {

    /**
     * 문항 DTO
     */
    public record Question(
            String questionId,
            String title,
            List<Option> options
    ) {
    }

    /**
     * 선택지 DTO
     */
    public record Option(
            String code,
            String label
    ) {
    }
}

