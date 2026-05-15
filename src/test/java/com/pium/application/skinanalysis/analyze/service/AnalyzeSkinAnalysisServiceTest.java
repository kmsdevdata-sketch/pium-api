package com.pium.application.skinanalysis.analyze.service;

import com.pium.adapter.outbound.skinanalysis.fixture.AnalyzeCommandFixture;
import com.pium.application.skinanalysis.analyze.required.dto.AnalyzedSkinMetrics;
import com.pium.adapter.outbound.skinanalysis.fixture.SkinAnalysisResultFixture;
import com.pium.application.skinanalysis.analyze.dto.AnalyzeCommand;
import com.pium.application.skinanalysis.analyze.dto.AnalyzeResultView;
import com.pium.application.skinanalysis.analyze.required.NormalizeSurveySubmissionPort;
import com.pium.application.skinanalysis.analyze.required.SaveSkinAnalysisResultPort;
import com.pium.application.skinanalysis.analyze.required.dto.NormalizeSurveySubmission;
import com.pium.domain.skinanalysis.SkinAnalysisEngine;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import com.pium.fixture.NormalizeSurveySubmissionFixture;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AnalyzeSkinAnalysisServiceTest {

    private final NormalizeSurveySubmissionPort normalizeSurveySubmissionPort = mock(NormalizeSurveySubmissionPort.class);
    private final SkinAnalysisEngine skinAnalysisEngine = mock(SkinAnalysisEngine.class);
    private final SaveSkinAnalysisResultPort saveSkinAnalysisResultPort = mock(SaveSkinAnalysisResultPort.class);

    private final AnalyzeSkinAnalysisService service = new AnalyzeSkinAnalysisService(
            normalizeSurveySubmissionPort,
            skinAnalysisEngine,
            saveSkinAnalysisResultPort
    );

    @Test
    void analyze_오케스트레이션_및_응답매핑_검증() {

        AnalyzeCommand command = AnalyzeCommandFixture.createAnalyzeCommand();
        NormalizeSurveySubmission normalized = NormalizeSurveySubmissionFixture.createNormalizeSurveySubmission();
        AnalyzedSkinMetrics engineResult = new AnalyzedSkinMetrics(SkinAnalysisResultFixture.createSkinMetricScores());

        when(normalizeSurveySubmissionPort.normalize(command)).thenReturn(normalized);
        when(skinAnalysisEngine.analyze(normalized)).thenReturn(engineResult);

        AnalyzeResultView view = service.analyze(command);

        InOrder inOrder = inOrder(normalizeSurveySubmissionPort, skinAnalysisEngine, saveSkinAnalysisResultPort);
        inOrder.verify(normalizeSurveySubmissionPort, times(1)).normalize(command);
        inOrder.verify(skinAnalysisEngine, times(1)).analyze(normalized);
        ArgumentCaptor<SkinAnalysisResult> resultCaptor = ArgumentCaptor.forClass(SkinAnalysisResult.class);
        inOrder.verify(saveSkinAnalysisResultPort, times(1)).save(resultCaptor.capture());

        SkinAnalysisResult savedResult = resultCaptor.getValue();

        assertThat(view.skinMetricScores()).hasSize(7);
        assertThat(view.skinMetricScores().get(0).metricKey()).isEqualTo("DRYNESS");
        assertThat(view.skinMetricScores().get(0).score()).isEqualTo(72);
        assertThat(savedResult.getUserId()).isEqualTo(command.userId());
        assertThat(savedResult.getGoals()).containsExactlyElementsOf(command.goals());
    } 
}
