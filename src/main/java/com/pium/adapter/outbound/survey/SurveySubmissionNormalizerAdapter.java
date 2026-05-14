package com.pium.adapter.outbound.survey;

import com.pium.application.skinanalysis.survey.provided.dto.AnalyzeCommand;
import com.pium.application.skinanalysis.survey.required.NormalizeSurveySubmissionPort;
import com.pium.application.skinanalysis.survey.required.dto.NormalizeSurveySubmission;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Outbound Adapter
 * - Inbound에서 들어온 설문 응답을 내부 표준 계약으로 정규화
 */

@Component
public class SurveySubmissionNormalizerAdapter implements NormalizeSurveySubmissionPort {


    @Override
    public NormalizeSurveySubmission normalize(AnalyzeCommand command) {
        List<NormalizeSurveySubmission.NormalizedAnswer> answers = command.answers().stream()
                .map(answer -> new NormalizeSurveySubmission.NormalizedAnswer(
                        answer.questionId(),
                        answer.selectedOptionCodes()
                ))
                .toList();
        return new NormalizeSurveySubmission(answers);
    }
}
