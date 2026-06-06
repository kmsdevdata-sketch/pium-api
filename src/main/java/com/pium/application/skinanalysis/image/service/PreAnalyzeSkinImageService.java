package com.pium.application.skinanalysis.image.service;

import com.pium.application.skinanalysis.image.dto.PreAnalyzeImageCommand;
import com.pium.application.skinanalysis.image.dto.PreAnalyzeImageView;
import com.pium.application.skinanalysis.image.dto.SkinImageFile;
import com.pium.application.skinanalysis.image.exception.ImageAnalysisApplicationErrorCode;
import com.pium.application.skinanalysis.image.exception.ImageAnalysisApplicationException;
import com.pium.application.skinanalysis.image.provided.PreAnalyzeSkinImage;
import com.pium.application.skinanalysis.image.required.AnalyzeSkinImagePort;
import com.pium.application.skinanalysis.spec.dto.SurveySpecView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PreAnalyzeSkinImageService implements PreAnalyzeSkinImage {

    private static final long MAX_IMAGE_SIZE_BYTES = 8L * 1024L * 1024L;

    private final AnalyzeSkinImagePort analyzeSkinImagePort;
    private final ImageAnalysisSessionStore imageAnalysisSessionStore;

    @Override
    public PreAnalyzeImageView preAnalyze(PreAnalyzeImageCommand command) {
        validateCommand(command);

        String sessionId = imageAnalysisSessionStore.start(
                command.userId(),
                () -> analyzeSkinImagePort.analyze(command.image())
        );

        return new PreAnalyzeImageView(sessionId, questions(), goalQuestion());
    }

    private void validateCommand(PreAnalyzeImageCommand command) {
        if (command == null || command.userId() == null || command.image() == null) {
            throw new ImageAnalysisApplicationException(ImageAnalysisApplicationErrorCode.INVALID_IMAGE_ANALYZE_COMMAND);
        }
        validateImage(command.image());
    }

    private void validateImage(SkinImageFile image) {
        if (
                image.bytes().length == 0 ||
                image.size() <= 0 ||
                image.size() > MAX_IMAGE_SIZE_BYTES ||
                image.contentType() == null ||
                !image.contentType().startsWith("image/")
        ) {
            throw new ImageAnalysisApplicationException(ImageAnalysisApplicationErrorCode.INVALID_IMAGE_ANALYZE_COMMAND);
        }
    }

    private List<SurveySpecView.Question> questions() {
        return List.of(
                question("IMG_DRYNESS_1", "세안 후 1시간 안에 얼굴이 당기거나 각질이 들뜨는 편인가요?"),
                question("IMG_OILINESS_1", "오후가 되면 이마·코 주변이나 얼굴 전체가 번들거리는 편인가요?"),
                question("IMG_SENSITIVITY_1", "새 화장품을 쓰면 따갑거나 붉어지는 편인가요?"),
                question("IMG_BLEMISH_1", "최근 2주 동안 트러블이 새로 올라오거나 반복되는 편인가요?")
        );
    }

    private SurveySpecView.Question goalQuestion() {
        return new SurveySpecView.Question(
                "IMG_GOAL_1",
                "지금 가장 신경 쓰이는 피부 고민은 무엇인가요? (최대 2개)",
                List.of(
                        new SurveySpecView.Option("Q11_1", "수분·장벽 안정"),
                        new SurveySpecView.Option("Q11_6", "진정·자극 완화"),
                        new SurveySpecView.Option("Q11_2", "트러블·피지 관리"),
                        new SurveySpecView.Option("Q11_3", "톤·잡티 케어"),
                        new SurveySpecView.Option("Q11_5", "탄력·주름 케어")
                )
        );
    }

    private SurveySpecView.Question question(String questionId, String title) {
        return new SurveySpecView.Question(
                questionId,
                title,
                List.of(
                        new SurveySpecView.Option(questionId + "_OPT_1", "전혀 아니에요"),
                        new SurveySpecView.Option(questionId + "_OPT_2", "드물게 그래요"),
                        new SurveySpecView.Option(questionId + "_OPT_3", "가끔 그래요"),
                        new SurveySpecView.Option(questionId + "_OPT_4", "자주 그래요"),
                        new SurveySpecView.Option(questionId + "_OPT_5", "거의 항상 그래요")
                )
        );
    }
}
