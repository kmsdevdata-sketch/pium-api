package com.pium.adapter.inbound.web.skinanalysis.survey;

import com.pium.application.skinanalysis.survey.provided.AnalyzeSkinAnalysis;
import com.pium.application.skinanalysis.survey.provided.GetSurveySpec;
import com.pium.application.skinanalysis.survey.provided.dto.SurveySpecView;
import com.pium.fixture.SurveySpecViewFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(SurveyController.class)
@WithMockUser
class SurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetSurveySpec getSurveySpec;

    @MockitoBean
    private AnalyzeSkinAnalysis analyzeSkinAnalysis;


    @Test
    void getSurveySpec_returnsApiResponse() throws Exception {

        SurveySpecView view = SurveySpecViewFixture.createSurveySpecView();

        given(getSurveySpec.getSurveySpec()).willReturn(view);

        mockMvc.perform(get("/api/v1/surveys") // perform = HTTP요청 시뮬레이션 메서드
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.questionResponses[0].questionId").value("Q_DRYNESS_1"))
                .andExpect(jsonPath("$.data.questionResponses[0].title").value("세안 후 얼굴이 당기나요?"))
                .andExpect(jsonPath("$.data.questionResponses[0].options[0].code").value("NONE"))
                .andExpect(jsonPath("$.data.questionResponses[0].options[0].label").value("거의 없음"))
                .andDo(print());

        verify(getSurveySpec, times(1)).getSurveySpec();
    }
}