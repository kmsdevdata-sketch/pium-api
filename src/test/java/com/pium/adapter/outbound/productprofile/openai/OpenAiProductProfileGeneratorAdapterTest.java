package com.pium.adapter.outbound.productprofile.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pium.adapter.outbound.productprofile.exception.ProductProfileAdapterException;
import com.pium.domain.product.fixture.ProductFixture;
import com.pium.domain.product.model.Product;
import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.productprofile.model.ProductProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiProductProfileGeneratorAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient.Builder restClientBuilder = RestClient.builder();
    private final OpenAiProductProfileProperties properties = new OpenAiProductProfileProperties(
            "test-api-key",
            "https://api.openai.test/v1/responses",
            "gpt-5.5"
    );

    private MockRestServiceServer server;
    private OpenAiProductProfileGeneratorAdapter adapter;

    @BeforeEach
    void setUp() {
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        adapter = new OpenAiProductProfileGeneratorAdapter(restClientBuilder, properties, objectMapper);
    }

    @Test
    void generate_OpenAI_응답을_ProductProfile로_변환한다() {
        Product product = ProductFixture.createProduct();
        server.expect(once(), requestTo(properties.responsesUri()))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-api-key"))
                .andRespond(withSuccess(openAiResponse(), MediaType.APPLICATION_JSON));

        ProductProfile result = adapter.generate(product);

        assertThat(result.productId()).isEqualTo(product.getId());
        assertThat(result.category()).isEqualTo(product.getCategory());
        assertThat(result.usageStep()).isEqualTo(product.getUsageStep());
        assertThat(result.benefitTraits().get(0).trait()).isEqualTo(RecommendationTrait.BARRIER_SUPPORT);
        assertThat(result.evidenceSignals()).hasSize(1);
        server.verify();
    }

    @Test
    void generate_응답_JSON이_유효하지_않으면_예외가_발생한다() {
        Product product = ProductFixture.createProduct();
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

        assertThatThrownBy(() -> adapter.generate(product))
                .isInstanceOf(ProductProfileAdapterException.class);
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
                          "text": "{\\"benefitTraits\\":[{\\"trait\\":\\"BARRIER_SUPPORT\\",\\"strength\\":\\"MODERATE\\",\\"confidence\\":\\"MEDIUM\\",\\"evidenceRefs\\":[\\"ev_1\\"]}],\\"riskTraits\\":[],\\"ingredientGroups\\":[\\"BARRIER_LIPID\\"],\\"activeFamilies\\":[\\"CERAMIDE\\"],\\"evidenceSignals\\":[{\\"id\\":\\"ev_1\\",\\"type\\":\\"INGREDIENT_PRESENT\\",\\"sourceField\\":\\"INGREDIENTS\\",\\"message\\":\\"세라마이드 계열 성분이 포함되어 있음\\",\\"confidence\\":\\"MEDIUM\\"}],\\"warnings\\":[\\"정확한 함량은 알 수 없음\\"]}"
                        }
                      ]
                    }
                  ]
                }
                """;
    }
}
