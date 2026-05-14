package com.pium.fixture;

import com.pium.application.skinanalysis.spec.dto.SurveySpecView;

import java.util.List;

public class SurveySpecViewFixture {

    public static SurveySpecView createSurveySpecView() {
        return new SurveySpecView(
                List.of(
                        new SurveySpecView.Question(
                                "Q_DRYNESS_1",
                                "세안 후 얼굴이 당기나요?",
                                List.of(
                                        new SurveySpecView.Option("NONE", "거의 없음"),
                                        new SurveySpecView.Option("ALWAYS", "거의 항상")
                                )
                        )
                ));
    }
}
