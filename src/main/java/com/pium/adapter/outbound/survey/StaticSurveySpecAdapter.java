package com.pium.adapter.outbound.survey;

import com.pium.adapter.outbound.survey.exception.SurveyAdapterErrorCode;
import com.pium.adapter.outbound.survey.exception.SurveyAdapterException;
import com.pium.application.skinanalysis.survey.provided.dto.SurveySpecView;
import com.pium.application.skinanalysis.survey.required.LoadSurveySpecPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class StaticSurveySpecAdapter implements LoadSurveySpecPort {

    @Override
    public Optional<SurveySpecView> loadCurrent() {
        try {
            return Optional.of(createSingleQuestionSpec(
                    "v1",
                    "Q_DRYNESS_1",
                    "세안 후 얼굴이 당기나요?",
                    frequencyOptions()
            ));
        } catch (Exception e) {
            throw new SurveyAdapterException(SurveyAdapterErrorCode.SURVEY_SPEC_LOAD_FAILED);
        }
    }

    private static SurveySpecView createSingleQuestionSpec(
            String version,
            String questionId,
            String title,
            List<SurveySpecView.Option> options
    ) {
        return new SurveySpecView(
                version,
                List.of(
                        new SurveySpecView.Question(
                                questionId,
                                title,
                                options
                        )
                )
        );
    }

    private static List<SurveySpecView.Option> frequencyOptions() {
        return List.of(
                new SurveySpecView.Option("NONE", "거의 없음"),
                new SurveySpecView.Option("SOMETIMES", "가끔"),
                new SurveySpecView.Option("NORMAL", "보통"),
                new SurveySpecView.Option("OFTEN", "자주"),
                new SurveySpecView.Option("ALWAYS", "거의 항상")
        );
    }
}
