package com.pium.adapter.outbound.skinanalysis.image.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pium.adapter.outbound.skinanalysis.exception.SkinAnalysisAdapterException;
import com.pium.application.skinanalysis.image.dto.SkinImageFile;
import com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiSkinImageAnalyzerAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient.Builder restClientBuilder = RestClient.builder();
    private final OpenAiSkinImageAnalysisProperties properties = new OpenAiSkinImageAnalysisProperties(
            "test-api-key",
            "https://api.openai.test/v1/responses",
            "gpt-5-nano"
    );

    private MockRestServiceServer server;
    private OpenAiSkinImageAnalyzerAdapter adapter;

    @BeforeEach
    void setUp() {
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        adapter = new OpenAiSkinImageAnalyzerAdapter(restClientBuilder, properties, objectMapper);
    }

    @Test
    void analyze_OpenAI_응답을_ImageSkinAnalysis로_변환한다() {
        server.expect(once(), requestTo(properties.responsesUri()))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-api-key"))
                .andExpect(content().string(containsString("\"model\":\"gpt-5-nano\"")))
                .andExpect(content().string(containsString("data:image/png;base64,")))
                .andRespond(withSuccess(openAiResponse(), MediaType.APPLICATION_JSON));

        ImageSkinAnalysis result = adapter.analyze(image());

        assertThat(result.imageQuality().usable()).isTrue();
        assertThat(result.visualSignals().blemish().score()).isEqualTo(68);
        assertThat(result.visualSignals().blemish().confidence()).isEqualTo(ImageSkinAnalysis.Confidence.MEDIUM);
        assertThat(result.warnings()).containsExactly("사진 조명에 따라 유분 판단이 달라질 수 있어요.");
        server.verify();
    }

    @Test
    void analyze_응답_JSON이_유효하지_않으면_예외가_발생한다() {
        server.expect(once(), requestTo(properties.responsesUri()))
                .andRespond(withSuccess("""
                        {
                          "status": "completed",
                          "output": [
                            {
                              "type": "message",
                              "content": [
                                {
                                  "type": "output_text",
                                  "text": "{not-json}"
                                }
                              ]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.analyze(image()))
                .isInstanceOf(SkinAnalysisAdapterException.class);
    }

    private SkinImageFile image() {
        return new SkinImageFile(
                "selfie.png",
                MediaType.IMAGE_PNG_VALUE,
                3,
                new byte[]{1, 2, 3}
        );
    }

    private String openAiResponse() {
        return """
                {
                  "status": "completed",
                  "output": [
                    {
                      "type": "message",
                      "content": [
                        {
                          "type": "output_text",
                          "text": "{\\"imageQuality\\":{\\"usable\\":true,\\"reasonCodes\\":[\\"NONE\\"]},\\"visualSignals\\":{\\"blemish\\":{\\"score\\":68,\\"confidence\\":\\"MEDIUM\\"},\\"pigmentationTone\\":{\\"score\\":52,\\"confidence\\":\\"MEDIUM\\"},\\"agingSigns\\":{\\"score\\":35,\\"confidence\\":\\"LOW\\"},\\"drynessHint\\":{\\"score\\":42,\\"confidence\\":\\"LOW\\"},\\"oilinessHint\\":{\\"score\\":28,\\"confidence\\":\\"LOW\\"},\\"rednessHint\\":{\\"score\\":45,\\"confidence\\":\\"LOW\\"}},\\"warnings\\":[\\"사진 조명에 따라 유분 판단이 달라질 수 있어요.\\"]}"
                        }
                      ]
                    }
                  ]
                }
                """;
    }
}
