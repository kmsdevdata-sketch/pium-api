package com.pium.domain.skinanalysis.engine;

import com.pium.application.skinanalysis.analyze.required.dto.NormalizeSurveySubmission;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 정규화된 설문 제출값을 조회 전용 컨텍스트로 변환한다.
 */
public class SurveyAnswerCollector {

    /**
     * 질문 ID 기준으로 응답 스냅샷을 구성한다.
     */
    public AnalysisContext collect(NormalizeSurveySubmission submission) {
        Map<String, QuestionAnswerSnapshot> answersByQuestionId = new LinkedHashMap<>();

        for (NormalizeSurveySubmission.NormalizedAnswer answer : submission.answers()) {
            QuestionAnswerSnapshot snapshot = QuestionAnswerSnapshot.from(answer);
            answersByQuestionId.put(snapshot.questionId(), snapshot);
        }

        return new AnalysisContext(answersByQuestionId);
    }
}
