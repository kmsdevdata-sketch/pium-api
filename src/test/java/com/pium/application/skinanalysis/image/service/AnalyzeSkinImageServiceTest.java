package com.pium.application.skinanalysis.image.service;

import com.pium.application.skinanalysis.analyze.required.SaveSkinAnalysisResultPort;
import com.pium.application.skinanalysis.image.dto.AnalyzeImageCommand;
import com.pium.application.skinanalysis.image.dto.AnalyzeImageResultView;
import com.pium.application.skinanalysis.image.exception.ImageAnalysisApplicationException;
import com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis;
import com.pium.domain.skinanalysis.enumtype.SkinAnalysisType;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis.Confidence.HIGH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AnalyzeSkinImageServiceTest {

    private final SaveSkinAnalysisResultPort saveSkinAnalysisResultPort = mock(SaveSkinAnalysisResultPort.class);
    private final ImageAnalysisSessionStore imageAnalysisSessionStore = new ImageAnalysisSessionStore();

    private final AnalyzeSkinImageService service = new AnalyzeSkinImageService(
            saveSkinAnalysisResultPort,
            imageAnalysisSessionStore
    );

    @AfterEach
    void tearDown() {
        imageAnalysisSessionStore.shutdown();
    }

    @Test
    void analyze_사진분석결과를_IMAGE_타입으로_저장한다() {
        UserId userId = UserId.of("user-test-001");
        String sessionId = imageAnalysisSessionStore.start(userId, () -> imageAnalysis(true));
        AnalyzeImageCommand command = command(userId, sessionId);

        AnalyzeImageResultView view = service.analyze(command);

        ArgumentCaptor<SkinAnalysisResult> resultCaptor = ArgumentCaptor.forClass(SkinAnalysisResult.class);
        verify(saveSkinAnalysisResultPort, times(1)).save(resultCaptor.capture());

        SkinAnalysisResult savedResult = resultCaptor.getValue();
        assertThat(savedResult.getUserId()).isEqualTo(command.userId());
        assertThat(savedResult.getAnalysisType()).isEqualTo(SkinAnalysisType.IMAGE);
        assertThat(savedResult.getGoals()).containsExactlyElementsOf(command.goals());
        assertThat(view.status()).isEqualTo(AnalyzeImageResultView.Status.COMPLETED);
        assertThat(view.skinMetricScores()).hasSize(7);
    }

    @Test
    void analyze_분석불가_사진이면_저장하지_않고_예외가_발생한다() {
        UserId userId = UserId.of("user-test-001");
        String sessionId = imageAnalysisSessionStore.start(userId, () -> imageAnalysis(false));
        AnalyzeImageCommand command = command(userId, sessionId);

        assertThatThrownBy(() -> service.analyze(command))
                .isInstanceOf(ImageAnalysisApplicationException.class);

        verify(saveSkinAnalysisResultPort, never()).save(any());
    }

    @Test
    void analyze_사진분석이_진행중이면_PROCESSING을_반환한다() throws InterruptedException {
        UserId userId = UserId.of("user-test-001");
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch completed = new CountDownLatch(1);
        String sessionId = imageAnalysisSessionStore.start(userId, () -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ImageSkinAnalysis result = imageAnalysis(true);
            completed.countDown();
            return result;
        });
        AnalyzeImageCommand command = command(userId, sessionId);

        AnalyzeImageResultView view = service.analyze(command);

        assertThat(view.status()).isEqualTo(AnalyzeImageResultView.Status.PROCESSING);
        assertThat(view.retryAfterSeconds()).isEqualTo(2);
        verify(saveSkinAnalysisResultPort, never()).save(any());

        latch.countDown();
        completed.await();
        waitUntilDone(sessionId);

        AnalyzeImageResultView completedView = service.analyze(command);

        assertThat(completedView.status()).isEqualTo(AnalyzeImageResultView.Status.COMPLETED);
        verify(saveSkinAnalysisResultPort, times(1)).save(any());
    }

    private AnalyzeImageCommand command(UserId userId, String sessionId) {
        return new AnalyzeImageCommand(
                userId,
                sessionId,
                List.of(
                        answer("IMG_DRYNESS_1", "IMG_DRYNESS_3"),
                        answer("IMG_OILINESS_1", "IMG_OILINESS_3"),
                        answer("IMG_SENSITIVITY_1", "IMG_SENSITIVITY_3"),
                        answer("IMG_BLEMISH_1", "IMG_BLEMISH_3")
                ),
                List.of("Q11_1", "Q11_2")
        );
    }

    private AnalyzeImageCommand.Answer answer(String questionId, String optionCode) {
        return new AnalyzeImageCommand.Answer(questionId, List.of(optionCode));
    }

    private ImageSkinAnalysis imageAnalysis(boolean usable) {
        return new ImageSkinAnalysis(
                new ImageSkinAnalysis.ImageQuality(usable, List.of()),
                new ImageSkinAnalysis.VisualSignals(
                        signal(55),
                        signal(55),
                        signal(55),
                        signal(55),
                        signal(55),
                        signal(55)
                ),
                List.of()
        );
    }

    private ImageSkinAnalysis.VisualSignal signal(int score) {
        return new ImageSkinAnalysis.VisualSignal(score, HIGH);
    }

    private void waitUntilDone(String sessionId) throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            if (imageAnalysisSessionStore.find(sessionId).orElseThrow().isDone()) {
                return;
            }
            Thread.sleep(10);
        }
    }
}
