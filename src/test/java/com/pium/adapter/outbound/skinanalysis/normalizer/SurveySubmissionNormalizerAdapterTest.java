package com.pium.adapter.outbound.skinanalysis.normalizer;

import com.pium.adapter.outbound.skinanalysis.exception.SkinAnalysisAdapterException;
import com.pium.adapter.outbound.skinanalysis.fixture.AnalyzeCommandFixture;
import com.pium.application.skinanalysis.analyze.dto.AnalyzeCommand;
import com.pium.application.skinanalysis.analyze.required.dto.NormalizeSurveySubmission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class SurveySubmissionNormalizerAdapterTest {

    AnalyzeCommand analyzeCommand;
    SurveySubmissionNormalizerAdapter normalizer;

    @BeforeEach
    void setUp() {
        analyzeCommand = AnalyzeCommandFixture.createAnalyzeCommand();
        normalizer = new SurveySubmissionNormalizerAdapter();
    }

    @Test
    void normalize_정상입력_정규화성공() {

        NormalizeSurveySubmission result = normalizer.normalize(analyzeCommand);

        assertThat(result.answers()).hasSize(2);
        assertThat(result.answers().get(0).questionId()).isEqualTo("Q_DRYNESS_1");
        assertThat(result.answers().get(0).selectedOptionCodes()).containsExactly("Q1_1");
        assertThat(result.goals()).containsExactly("Q11_1", "Q11_2");
    }

    @Test
    void normalize_command_null_예외() {
        assertThatThrownBy(() -> normalizer.normalize(null))
                .isInstanceOf(SkinAnalysisAdapterException.class);
    }

    @Test
    void normalize_answers_empty_예외() {
        AnalyzeCommand command = new AnalyzeCommand(List.of(), List.of("Q11_1"));

        assertThatThrownBy(() -> normalizer.normalize(command))
                .isInstanceOf(SkinAnalysisAdapterException.class);
    }


    @Test
    void normalize_goals_null_예외() {
        AnalyzeCommand command = new AnalyzeCommand(
                List.of(new AnalyzeCommand.Answer("Q_DRYNESS_1", List.of("Q1_1"))),
                null
        );

        assertThatThrownBy(() -> normalizer.normalize(command))
                .isInstanceOf(SkinAnalysisAdapterException.class);
    }

    @Test
    void normalize_goals_empty_예외() {
        AnalyzeCommand command = new AnalyzeCommand(
                List.of(new AnalyzeCommand.Answer("Q_DRYNESS_1", List.of("Q1_1"))),
                List.of()
        );

        assertThatThrownBy(() -> normalizer.normalize(command))
                .isInstanceOf(SkinAnalysisAdapterException.class);
    }

    @Test
    void normalize_questionId_blank_예외() {
        AnalyzeCommand command = new AnalyzeCommand(
                List.of(new AnalyzeCommand.Answer("   ", List.of("Q1_1"))),
                List.of("Q11_1")
        );

        assertThatThrownBy(() -> normalizer.normalize(command))
                .isInstanceOf(SkinAnalysisAdapterException.class);
    }

    @Test
    void normalize_optionCodes_all_invalid_예외() {
        AnalyzeCommand command = new AnalyzeCommand(
                List.of(new AnalyzeCommand.Answer("Q_DRYNESS_1", List.of(" ", "", "   "))),
                List.of("Q11_1")
        );

        assertThatThrownBy(() -> normalizer.normalize(command))
                .isInstanceOf(SkinAnalysisAdapterException.class);
    }
}