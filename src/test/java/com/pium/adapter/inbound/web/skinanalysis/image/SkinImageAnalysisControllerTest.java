package com.pium.adapter.inbound.web.skinanalysis.image;

import com.pium.adapter.inbound.web.auth.AuthenticatedUserIdResolver;
import com.pium.application.skinanalysis.image.dto.AnalyzeImageResultView;
import com.pium.application.skinanalysis.image.dto.PreAnalyzeImageCommand;
import com.pium.application.skinanalysis.image.dto.PreAnalyzeImageView;
import com.pium.application.skinanalysis.image.provided.AnalyzeSkinImage;
import com.pium.application.skinanalysis.image.provided.PreAnalyzeSkinImage;
import com.pium.application.skinanalysis.spec.dto.SurveySpecView;
import com.pium.domain.user.vo.UserId;
import com.pium.fixture.AnalyzeResultViewFixture;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SkinImageAnalysisController.class)
@WithMockUser
class SkinImageAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PreAnalyzeSkinImage preAnalyzeSkinImage;

    @MockitoBean
    private AnalyzeSkinImage analyzeSkinImage;

    @MockitoBean
    private AuthenticatedUserIdResolver authenticatedUserIdResolver;

    @Test
    void preAnalyze_multipart_사진으로_선분석을_시작한다() throws Exception {
        PreAnalyzeImageView view = new PreAnalyzeImageView(
                "session-001",
                List.of(question("IMG_DRYNESS_1")),
                question("IMG_GOAL_1")
        );
        given(authenticatedUserIdResolver.resolve(any())).willReturn(UserId.of("user-test-001"));
        given(preAnalyzeSkinImage.preAnalyze(any(PreAnalyzeImageCommand.class))).willReturn(view);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "selfie.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/v1/skin-images/pre-analyze")
                        .file(image)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.analysisSessionId").value("session-001"))
                .andExpect(jsonPath("$.data.questions[0].questionId").value("IMG_DRYNESS_1"))
                .andExpect(jsonPath("$.data.goalQuestion.questionId").value("IMG_GOAL_1"));

        ArgumentCaptor<PreAnalyzeImageCommand> commandCaptor = ArgumentCaptor.forClass(PreAnalyzeImageCommand.class);
        verify(preAnalyzeSkinImage, times(1)).preAnalyze(commandCaptor.capture());
        assertThat(commandCaptor.getValue().userId()).isEqualTo(UserId.of("user-test-001"));
        assertThat(commandCaptor.getValue().image().contentType()).isEqualTo(MediaType.IMAGE_JPEG_VALUE);
    }

    @Test
    void analyze_json_세션과_보조문항으로_최종분석한다() throws Exception {
        AnalyzeImageResultView view = AnalyzeImageResultView.completed(AnalyzeResultViewFixture.createAnalyzeResultView());
        given(authenticatedUserIdResolver.resolve(any())).willReturn(UserId.of("user-test-001"));
        given(analyzeSkinImage.analyze(any())).willReturn(view);

        mockMvc.perform(post("/api/v1/skin-images/analyze")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(analyzeRequestJson())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.skinMetricScores[0].metricKey").value("DRYNESS"))
                .andExpect(jsonPath("$.data.skinMetricScores[0].score").value(72));

        verify(authenticatedUserIdResolver, times(1)).resolve(any());
        verify(analyzeSkinImage, times(1)).analyze(any());
    }

    @Test
    void analyze_이미지분석_진행중이면_202를_반환한다() throws Exception {
        given(authenticatedUserIdResolver.resolve(any())).willReturn(UserId.of("user-test-001"));
        given(analyzeSkinImage.analyze(any())).willReturn(AnalyzeImageResultView.processing());

        mockMvc.perform(post("/api/v1/skin-images/analyze")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(analyzeRequestJson())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"))
                .andExpect(jsonPath("$.data.retryAfterSeconds").value(2));
    }

    private SurveySpecView.Question question(String questionId) {
        return new SurveySpecView.Question(
                questionId,
                "질문",
                List.of(new SurveySpecView.Option(questionId + "_OPT_1", "선택지"))
        );
    }

    private String analyzeRequestJson() {
        return """
                {
                  "analysisSessionId": "session-001",
                  "answers": [
                    { "questionId": "IMG_DRYNESS_1", "selectedOptionCodes": ["IMG_DRYNESS_1_OPT_3"] },
                    { "questionId": "IMG_OILINESS_1", "selectedOptionCodes": ["IMG_OILINESS_1_OPT_3"] },
                    { "questionId": "IMG_SENSITIVITY_1", "selectedOptionCodes": ["IMG_SENSITIVITY_1_OPT_3"] },
                    { "questionId": "IMG_BLEMISH_1", "selectedOptionCodes": ["IMG_BLEMISH_1_OPT_3"] }
                  ],
                  "goals": ["Q11_1", "Q11_2"]
                }
                """;
    }
}
