package com.pium.adapter.inbound.web.skinanalysis.survey;

import com.pium.adapter.inbound.web.skinanalysis.spec.SurveySpecResponse;
import com.pium.application.skinanalysis.spec.dto.SurveySpecView;
import com.pium.fixture.SurveySpecViewFixture;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SurveySpecResponseTest {

    @Test
    void from_mapping_accuracy() {

        SurveySpecView view = SurveySpecViewFixture.createSurveySpecView();

        SurveySpecResponse response = SurveySpecResponse.from(view);

        assertThat(response).isNotNull();
        assertThat(response.questionResponses()).hasSize(view.questions().size());

        for (int i = 0; i < view.questions().size(); i++) {
            SurveySpecView.Question expectedQuestion = view.questions().get(i);
            SurveySpecResponse.QuestionResponse actualQuestion = response.questionResponses().get(i);

            assertThat(actualQuestion.questionId()).isEqualTo(expectedQuestion.questionId());
            assertThat(actualQuestion.title()).isEqualTo(expectedQuestion.title());
            assertThat(actualQuestion.options()).hasSize(expectedQuestion.options().size());

            for (int j = 0; j < expectedQuestion.options().size(); j++) {
                SurveySpecView.Option expectedOption = expectedQuestion.options().get(j);
                SurveySpecResponse.OptionResponse actualOption = actualQuestion.options().get(j);

                assertThat(actualOption.code()).isEqualTo(expectedOption.code());
                assertThat(actualOption.label()).isEqualTo(expectedOption.label());
            }
        }
    }

}