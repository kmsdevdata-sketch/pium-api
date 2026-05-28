package com.pium.adapter.outbound.productprofile.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pium.adapter.outbound.productprofile.exception.ProductProfileAdapterErrorCode;
import com.pium.adapter.outbound.productprofile.exception.ProductProfileAdapterException;
import com.pium.application.productprofile.required.GenerateProductProfilePort;
import com.pium.domain.product.model.Product;
import com.pium.domain.productprofile.model.ProductProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * OpenAI API 기반 ProductProfile 생성 어댑터.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiProductProfileGeneratorAdapter implements GenerateProductProfilePort {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String OUTPUT_TEXT = "output_text";
    private static final String REFUSAL = "refusal";

    private final RestClient.Builder restClientBuilder;
    private final OpenAiProductProfileProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public ProductProfile generate(Product product) {
        OpenAiResponsesApiResponse response = requestProfile(product);
        String outputText = extractOutputText(response);
        OpenAiProductProfileDraft draft = parseDraft(outputText);
        return draft.toDomain(product.getId(), product.getCategory(), product.getUsageStep());
    }

    private OpenAiResponsesApiResponse requestProfile(Product product) {
        try {
            return restClientBuilder.build()
                    .post()
                    .uri(properties.responsesUri())
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody(product))
                    .retrieve()
                    .body(OpenAiResponsesApiResponse.class);
        } catch (RestClientResponseException e) {
            log.warn(
                    "OpenAI product profile generation HTTP error. status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );
            throw new ProductProfileAdapterException(ProductProfileAdapterErrorCode.PRODUCT_PROFILE_GENERATION_FAILED);
        }
    }

    private Map<String, Object> requestBody(Product product) {
        return Map.of(
                "model", properties.model(),
                "store", false,
                "instructions", instructions(),
                "input", List.of(userInput(product)),
                "text", Map.of("format", OpenAiProductProfileSchema.responseFormat())
        );
    }

    private Map<String, Object> userInput(Product product) {
        return Map.of(
                "role", "user",
                "content", List.of(Map.of(
                        "type", "input_text",
                        "text", productInputJson(product)
                ))
        );
    }

    private String productInputJson(Product product) {
        Map<String, Object> input = Map.of(
                "brandName", nullToEmpty(product.getBrandName()),
                "productName", nullToEmpty(product.getProductName()),
                "category", product.getCategory().name(),
                "usageStep", product.getUsageStep().name(),
                "functionalLabels", product.getFunctionalLabels().stream()
                        .map(Enum::name)
                        .toList(),
                "ingredientText", nullToEmpty(product.getIngredientText()),
                "claims", nullToEmpty(product.getClaims())
        );

        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new ProductProfileAdapterException(ProductProfileAdapterErrorCode.PRODUCT_PROFILE_GENERATION_FAILED);
        }
    }

    private String instructions() {
        return """
                You create ProductProfile JSON for a cosmetic recommendation system.
                Use only the provided product input fields. Do not browse the internet or infer external product facts.
                Brand and product names are weak context only; do not create strong benefit claims from names alone.
                Do not infer concentration, pH, high-dose, low-pH, or clinical efficacy unless explicitly present in labels, ingredients, or claims.
                If evidence is weak, use LOW confidence or add a warning.
                Every benefitTraits and riskTraits item must reference existing evidenceSignals ids.
                Use empty arrays when there is no grounded signal.
                Return Korean evidence messages and warnings.
                """.trim();
    }

    private String extractOutputText(OpenAiResponsesApiResponse response) {
        if (response == null || response.output() == null) {
            throw new ProductProfileAdapterException(ProductProfileAdapterErrorCode.PRODUCT_PROFILE_RESPONSE_INVALID);
        }

        for (OpenAiResponsesApiResponse.Output output : response.output()) {
            if (output.content() == null) {
                continue;
            }
            for (OpenAiResponsesApiResponse.Content content : output.content()) {
                if (REFUSAL.equals(content.type())) {
                    throw new ProductProfileAdapterException(ProductProfileAdapterErrorCode.PRODUCT_PROFILE_RESPONSE_INVALID);
                }
                if (OUTPUT_TEXT.equals(content.type()) && content.text() != null && !content.text().isBlank()) {
                    return content.text();
                }
            }
        }

        throw new ProductProfileAdapterException(ProductProfileAdapterErrorCode.PRODUCT_PROFILE_RESPONSE_INVALID);
    }

    private OpenAiProductProfileDraft parseDraft(String outputText) {
        try {
            return objectMapper.readValue(outputText, OpenAiProductProfileDraft.class);
        } catch (JsonProcessingException e) {
            log.warn("OpenAI product profile response parse failed. body={}", outputText);
            throw new ProductProfileAdapterException(ProductProfileAdapterErrorCode.PRODUCT_PROFILE_RESPONSE_INVALID);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
