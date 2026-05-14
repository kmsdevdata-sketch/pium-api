package com.pium.application.skinanalysis.analyze.service;

import com.pium.application.skinanalysis.analyze.provided.AnalyzeSkinAnalysis;
import com.pium.application.skinanalysis.analyze.dto.AnalyzeCommand;
import com.pium.application.skinanalysis.analyze.dto.AnalyzeResultView;
import com.pium.application.skinanalysis.analyze.required.NormalizeSurveySubmissionPort;
import com.pium.application.skinanalysis.analyze.required.SaveSkinAnalysisResultPort;
import com.pium.application.skinanalysis.analyze.required.dto.NormalizeSurveySubmission;
import com.pium.domain.skinanalysis.SkinAnalysisEngine;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provided Port 구현체
 * - [설문 응답 -> 정규화 -> 분석 -> 저장 -> 응답 변환) 오케스트레이션
 */
@Service
@RequiredArgsConstructor
public class AnalyzeSkinAnalysisService implements AnalyzeSkinAnalysis {

    private final NormalizeSurveySubmissionPort normalizeSurveySubmissionPort;
    private final SkinAnalysisEngine skinAnalysisEngine;
    private final SaveSkinAnalysisResultPort saveSkinAnalysisResultPort;


    @Override
    public AnalyzeResultView analyze(AnalyzeCommand command) {

        NormalizeSurveySubmission normalize = normalizeSurveySubmissionPort.normalize(command);
        SkinAnalysisResult result = skinAnalysisEngine.analyze(normalize);
        saveSkinAnalysisResultPort.save(result);

        List<AnalyzeResultView.SkinMetricScoreView> scoreViews = getSkinMetricScoreViews(result);

        return new AnalyzeResultView(scoreViews);
    }

    private static List<AnalyzeResultView.SkinMetricScoreView> getSkinMetricScoreViews(SkinAnalysisResult result) {
        List<AnalyzeResultView.SkinMetricScoreView> scoreViews = result.getSkinMetricScores().stream()
                .map(score -> new AnalyzeResultView.SkinMetricScoreView(
                                score.metric().name(),
                                score.score()
                        )
                ).toList();
        return scoreViews;
    }
}
