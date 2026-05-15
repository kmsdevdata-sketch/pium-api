package com.pium.domain.skinanalysis.engine;

import java.util.Map;
import java.util.Set;

/**
 * SkinAnalysis 엔진 내부에서 사용하는 조회 전용 입력 컨텍스트다.
 */
public record AnalysisContext(
        Map<String, QuestionAnswerSnapshot> answersByQuestionId
) {

    /**
     * 특정 질문의 응답 스냅샷 반환
     *
     * @param questionId 질문 식별자
     * @return 해장 질문의 응답 스냅샷, 없으면 null
     */
    QuestionAnswerSnapshot answerOf(String questionId) {
        return answersByQuestionId.get(questionId);
    }

    /**
     * 특정 질문에 옵션 코드가 선택되었는지 확인
     */
    boolean hasOption(String questionId, String optionCode) {
        QuestionAnswerSnapshot snapshot = answersByQuestionId.get(questionId);
        return snapshot != null && snapshot.contains(optionCode);
    }

    /**
     * 특정 질문의 선택 옵션 코드 집합을 반환
     */
    Set<String> optionCodesOf(String questionId) {
        QuestionAnswerSnapshot snapshot = answersByQuestionId.get(questionId);
        return snapshot == null ? Set.of() : snapshot.selectedOptionCodes();
    }

    /**
     * 특정 질문의 첫 번째 선택 옵션 코드를 반환
     *
     * <p>단일 선택 문항에서 자주 쓰기 위한 메서드</p>
     */
    String firstOptionOf(String questionId) {
        QuestionAnswerSnapshot snapshot = answersByQuestionId.get(questionId);
        return snapshot == null ? null : snapshot.firstOptionCode();
    }

    /**
     * 특정 질문에 응답이 존재하는지 확인
     */
    boolean hasAnswer(String questionId) {
        return answersByQuestionId.containsKey(questionId);
    }
}
