package com.pium.application.skinanalysis.image.service;

import com.pium.application.skinanalysis.image.dto.PreAnalyzeImageCommand;
import com.pium.application.skinanalysis.image.dto.PreAnalyzeImageView;
import com.pium.application.skinanalysis.image.dto.SkinImageFile;
import com.pium.application.skinanalysis.image.required.AnalyzeSkinImagePort;
import com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis.Confidence.HIGH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PreAnalyzeSkinImageServiceTest {

    private final AnalyzeSkinImagePort analyzeSkinImagePort = mock(AnalyzeSkinImagePort.class);
    private final ImageAnalysisSessionStore imageAnalysisSessionStore = new ImageAnalysisSessionStore();

    private final PreAnalyzeSkinImageService service = new PreAnalyzeSkinImageService(
            analyzeSkinImagePort,
            imageAnalysisSessionStore
    );

    @AfterEach
    void tearDown() {
        imageAnalysisSessionStore.shutdown();
    }

    @Test
    void preAnalyze_세션을_만들고_보조문항을_반환한다() {
        SkinImageFile image = new SkinImageFile("selfie.jpg", "image/jpeg", 3, new byte[]{1, 2, 3});
        when(analyzeSkinImagePort.analyze(image)).thenReturn(imageAnalysis());

        PreAnalyzeImageView view = service.preAnalyze(new PreAnalyzeImageCommand(
                UserId.of("user-test-001"),
                image
        ));

        assertThat(view.analysisSessionId()).isNotBlank();
        assertThat(view.questions()).hasSize(4);
        assertThat(view.questions().get(0).questionId()).isEqualTo("IMG_DRYNESS_1");
        assertThat(view.goalQuestion().questionId()).isEqualTo("IMG_GOAL_1");
        verify(analyzeSkinImagePort, timeout(1000)).analyze(image);
    }

    private ImageSkinAnalysis imageAnalysis() {
        return new ImageSkinAnalysis(
                new ImageSkinAnalysis.ImageQuality(true, List.of()),
                new ImageSkinAnalysis.VisualSignals(
                        signal(),
                        signal(),
                        signal(),
                        signal(),
                        signal(),
                        signal()
                ),
                List.of()
        );
    }

    private ImageSkinAnalysis.VisualSignal signal() {
        return new ImageSkinAnalysis.VisualSignal(55, HIGH);
    }
}
