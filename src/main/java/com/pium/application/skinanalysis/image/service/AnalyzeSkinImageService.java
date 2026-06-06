package com.pium.application.skinanalysis.image.service;

import com.pium.application.skinanalysis.analyze.dto.AnalyzeResultView;
import com.pium.application.skinanalysis.analyze.required.SaveSkinAnalysisResultPort;
import com.pium.application.skinanalysis.analyze.required.dto.AnalyzedSkinMetrics;
import com.pium.application.skinanalysis.image.dto.AnalyzeImageCommand;
import com.pium.application.skinanalysis.image.dto.AnalyzeImageResultView;
import com.pium.application.skinanalysis.image.exception.ImageAnalysisApplicationErrorCode;
import com.pium.application.skinanalysis.image.exception.ImageAnalysisApplicationException;
import com.pium.application.skinanalysis.image.provided.AnalyzeSkinImage;
import com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis;
import com.pium.domain.skinanalysis.enumtype.SkinAnalysisType;
import com.pium.domain.skinanalysis.image.ImageSkinAnalysisEngine;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletionException;

@Service
@RequiredArgsConstructor
public class AnalyzeSkinImageService implements AnalyzeSkinImage {

    private final SaveSkinAnalysisResultPort saveSkinAnalysisResultPort;
    private final ImageAnalysisSessionStore imageAnalysisSessionStore;

    private final ImageSkinAnalysisEngine imageSkinAnalysisEngine = new ImageSkinAnalysisEngine();

    @Override
    public AnalyzeImageResultView analyze(AnalyzeImageCommand command) {
        validateCommand(command);

        ImageAnalysisSession session = imageAnalysisSessionStore.find(command.analysisSessionId())
                .orElseThrow(() -> new ImageAnalysisApplicationException(ImageAnalysisApplicationErrorCode.IMAGE_ANALYSIS_SESSION_EXPIRED));
        validateSessionOwner(command, session);

        if (!session.isDone()) {
            return AnalyzeImageResultView.processing();
        }
        consumeSession(session);

        ImageSkinAnalysis imageAnalysis = imageAnalysis(session);
        validateImageAnalysis(imageAnalysis);

        AnalyzedSkinMetrics analyzed = imageSkinAnalysisEngine.analyze(imageAnalysis, command.answers());
        SkinAnalysisResult result = SkinAnalysisResult.create(
                command.userId(),
                analyzed.skinMetricScores(),
                command.goals(),
                SkinAnalysisType.IMAGE
        );
        saveSkinAnalysisResultPort.save(result);
        imageAnalysisSessionStore.remove(session.sessionId());

        AnalyzeResultView view = new AnalyzeResultView(result.getSkinMetricScores().stream()
                .map(score -> new AnalyzeResultView.SkinMetricScoreView(
                        score.metric().name(),
                        score.score()
                ))
                .toList());
        return AnalyzeImageResultView.completed(view);
    }

    private static void validateImageAnalysis(ImageSkinAnalysis imageAnalysis) {
        if (imageAnalysis == null || imageAnalysis.imageQuality() == null || !imageAnalysis.imageQuality().usable()) {
            throw new ImageAnalysisApplicationException(ImageAnalysisApplicationErrorCode.IMAGE_UNANALYZABLE);
        }
    }

    private void validateCommand(AnalyzeImageCommand command) {
        if (
                command == null ||
                command.userId() == null ||
                command.analysisSessionId() == null ||
                command.analysisSessionId().isBlank() ||
                command.answers() == null ||
                command.goals() == null ||
                command.goals().isEmpty()
        ) {
            throw new ImageAnalysisApplicationException(ImageAnalysisApplicationErrorCode.INVALID_IMAGE_ANALYZE_COMMAND);
        }

        validateRequiredAnswers(command.answers());
    }

    private void validateRequiredAnswers(List<AnalyzeImageCommand.Answer> answers) {
        if (answers.stream().anyMatch(answer ->
                answer == null ||
                answer.questionId() == null ||
                answer.questionId().isBlank() ||
                answer.selectedOptionCodes() == null ||
                answer.selectedOptionCodes().isEmpty()
        )) {
            throw new ImageAnalysisApplicationException(ImageAnalysisApplicationErrorCode.INVALID_IMAGE_ANALYZE_COMMAND);
        }
    }

    private void validateSessionOwner(AnalyzeImageCommand command, ImageAnalysisSession session) {
        if (!session.belongsTo(command.userId())) {
            throw new ImageAnalysisApplicationException(ImageAnalysisApplicationErrorCode.IMAGE_ANALYSIS_SESSION_FORBIDDEN);
        }
    }

    private void consumeSession(ImageAnalysisSession session) {
        if (!session.markConsumed()) {
            throw new ImageAnalysisApplicationException(ImageAnalysisApplicationErrorCode.IMAGE_ANALYSIS_SESSION_EXPIRED);
        }
    }

    private ImageSkinAnalysis imageAnalysis(ImageAnalysisSession session) {
        try {
            return session.join();
        } catch (CompletionException e) {
            imageAnalysisSessionStore.remove(session.sessionId());
            throw new ImageAnalysisApplicationException(ImageAnalysisApplicationErrorCode.IMAGE_ANALYSIS_FAILED);
        }
    }
}
