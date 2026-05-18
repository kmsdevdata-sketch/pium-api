package com.pium.adapter.inbound.web.skinanalysis.survey;

import com.pium.adapter.inbound.web.auth.AuthenticatedUserIdResolver;
import com.pium.adapter.inbound.web.skinanalysis.SurveyController;
import com.pium.application.skinanalysis.analyze.dto.AnalyzeCommand;
import com.pium.application.skinanalysis.analyze.dto.AnalyzeResultView;
import com.pium.application.skinanalysis.analyze.provided.AnalyzeSkinAnalysis;
import com.pium.application.skinanalysis.spec.provided.GetSurveySpec;
import com.pium.application.skinanalysis.spec.dto.SurveySpecView;
import com.pium.domain.user.vo.UserId;
import com.pium.fixture.AnalyzeResultViewFixture;
import com.pium.fixture.SurveySpecViewFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    @MockitoBean
    private AuthenticatedUserIdResolver authenticatedUserIdResolver;

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

    @Test
    void analyze_returnsApiResponse() throws Exception {
        AnalyzeResultView view = AnalyzeResultViewFixture.createAnalyzeResultView();

        given(authenticatedUserIdResolver.resolve(any())).willReturn(UserId.of("user-test-001"));
        given(analyzeSkinAnalysis.analyze(any(AnalyzeCommand.class))).willReturn(view);

        String requestJson = """
        {
          "answers": [
            { "questionId": "Q_DRYNESS_1", "selectedOptionCodes": ["Q1_2"] },
            { "questionId": "Q_DRYNESS_2", "selectedOptionCodes": ["Q2_1"] }
          ],
          "goals": ["Q11_1", "Q11_2"]
        }
        """;

        mockMvc.perform(post("/api/v1/surveys/analyze")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.skinMetricScores[0].metricKey").value("DRYNESS"))
                .andExpect(jsonPath("$.data.skinMetricScores[0].score").value(72))
                .andExpect(jsonPath("$.data.skinMetricScores[1].metricKey").value("BARRIER"))
                .andExpect(jsonPath("$.data.skinMetricScores[1].score").value(60));

        verify(authenticatedUserIdResolver, times(1)).resolve(any());
        verify(analyzeSkinAnalysis, times(1)).analyze(any(AnalyzeCommand.class));
    }

}
