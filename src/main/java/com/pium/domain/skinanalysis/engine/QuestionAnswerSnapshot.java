package com.pium.domain.skinanalysis.engine;

import com.pium.application.skinanalysis.analyze.required.dto.NormalizeSurveySubmission;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record QuestionAnswerSnapshot(
        String questionId,
        List<String> selectedOptionCodeList, // 순서 보존을 위해서 + 단일 문항에서 빠르게 조회
        Set<String> selectedOptionCodes // 포함 여부를 빠르게 조회 하기 위해서 + 매번 순회 방지
) {
    /**
     * 정규화된 응답 하나를 엔진 내부 스냅샷으로 변환한다.
     *
     */
    static QuestionAnswerSnapshot from(NormalizeSurveySubmission.NormalizedAnswer answer) {
        List<String> optionCodeList = List.copyOf(answer.selectedOptionCodes());
        Set<String> optionCodeSet = Set.copyOf(new LinkedHashSet<>(optionCodeList));

        return new QuestionAnswerSnapshot(
                answer.questionId(),
                optionCodeList,
                optionCodeSet
        );
    }


    /**
     * 특정 옵션 코드가 해당 질문에 포함되었는지 확인
     */
    boolean contains(String optionCode) {
        return selectedOptionCodes.contains(optionCode);
    }

    /**
     * 첫번째 선택 옵션 코드를 반환
     */
    public String firstOptionCode() {
        return selectedOptionCodeList.isEmpty() ? null : selectedOptionCodeList.getFirst();
    }
}
