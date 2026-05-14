package com.pium.adapter.outbound.skinanalysis.normalizer;

import com.pium.adapter.outbound.skinanalysis.exception.SkinAnalysisAdapterErrorCode;
import com.pium.adapter.outbound.skinanalysis.exception.SkinAnalysisAdapterException;
import com.pium.application.skinanalysis.analyze.dto.AnalyzeCommand;
import com.pium.application.skinanalysis.analyze.required.NormalizeSurveySubmissionPort;
import com.pium.application.skinanalysis.analyze.required.dto.NormalizeSurveySubmission;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Outbound Adapter
 * - Inbound에서 들어온 설문 응답을 내부 표준 계약으로 정규화
 */
@Component
public class SurveySubmissionNormalizerAdapter implements NormalizeSurveySubmissionPort {

    @Override
    public NormalizeSurveySubmission normalize(AnalyzeCommand command) {
        validateCommand(command);
        validateGoals(command.goals());

        List<NormalizeSurveySubmission.NormalizedAnswer> answers = command.answers().stream()
                .map(this::normalizeAnswer)
                .toList();

        return new NormalizeSurveySubmission(answers,command.goals());
    }

    private void validateGoals(List<String> goals) {
        if (goals == null || goals.isEmpty()) {
            throw new SkinAnalysisAdapterException(SkinAnalysisAdapterErrorCode.INVALID_ANSWER_PAYLOAD);
        }
    }

    private NormalizeSurveySubmission.NormalizedAnswer normalizeAnswer(AnalyzeCommand.Answer answer) {
        validateAnswer(answer);

        String normalizedQuestionId = answer.questionId().trim();
        List<String> normalizedOptionCodes = normalizeOptionCodes(answer.selectedOptionCodes());

        return new NormalizeSurveySubmission.NormalizedAnswer(
                normalizedQuestionId,
                normalizedOptionCodes
        );
    }

    private void validateCommand(AnalyzeCommand command) {
        if (command == null) {
            throw new SkinAnalysisAdapterException(SkinAnalysisAdapterErrorCode.INVALID_ANALYZE_COMMAND);
        }
        if (command.answers() == null || command.answers().isEmpty()) {
            throw new SkinAnalysisAdapterException(SkinAnalysisAdapterErrorCode.INVALID_ANALYZE_COMMAND);
        }
    }

    private void validateAnswer(AnalyzeCommand.Answer answer) {
        if (answer == null) {
            throw new SkinAnalysisAdapterException(SkinAnalysisAdapterErrorCode.INVALID_ANSWER_PAYLOAD);
        }
        if (answer.questionId() == null || answer.questionId().isBlank()) {
            throw new SkinAnalysisAdapterException(SkinAnalysisAdapterErrorCode.INVALID_ANSWER_PAYLOAD);
        }
        if (answer.selectedOptionCodes() == null || answer.selectedOptionCodes().isEmpty()) {
            throw new SkinAnalysisAdapterException(SkinAnalysisAdapterErrorCode.INVALID_OPTION_CODES);
        }
    }

    private List<String> normalizeOptionCodes(List<String> optionCodes) {
        List<String> normalized = optionCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .distinct()
                .toList();

        if (normalized.isEmpty()) {
            throw new SkinAnalysisAdapterException(SkinAnalysisAdapterErrorCode.INVALID_OPTION_CODES);
        }

        return normalized;
    }
}
