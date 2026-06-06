package com.pium.adapter.outbound.skinanalysis.image.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pium.adapter.outbound.skinanalysis.exception.SkinAnalysisAdapterErrorCode;
import com.pium.adapter.outbound.skinanalysis.exception.SkinAnalysisAdapterException;
import com.pium.application.skinanalysis.image.dto.SkinImageFile;
import com.pium.application.skinanalysis.image.required.AnalyzeSkinImagePort;
import com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiSkinImageAnalyzerAdapter implements AnalyzeSkinImagePort {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String OUTPUT_TEXT = "output_text";
    private static final String REFUSAL = "refusal";

    private final RestClient.Builder restClientBuilder;
    private final OpenAiSkinImageAnalysisProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public ImageSkinAnalysis analyze(SkinImageFile image) {
        OpenAiSkinImageAnalysisResponse response = requestAnalysis(image);
        String outputText = extractOutputText(response);
        OpenAiSkinImageAnalysisDraft draft = parseDraft(outputText);
        return draft.toApplicationDto();
    }

    private OpenAiSkinImageAnalysisResponse requestAnalysis(SkinImageFile image) {
        try {
            return restClientBuilder.build()
                    .post()
                    .uri(properties.responsesUri())
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody(image))
                    .retrieve()
                    .body(OpenAiSkinImageAnalysisResponse.class);
        } catch (RestClientResponseException e) {
            log.warn(
                    "OpenAI skin image analysis HTTP error. status={}",
                    e.getStatusCode()
            );
            throw new SkinAnalysisAdapterException(SkinAnalysisAdapterErrorCode.IMAGE_ANALYSIS_FAILED);
        }
    }

    private Map<String, Object> requestBody(SkinImageFile image) {
        return Map.of(
                "model", properties.model(),
                "store", false,
                "instructions", instructions(),
                "input", List.of(userInput(image)),
                "text", Map.of("format", OpenAiSkinImageAnalysisSchema.responseFormat())
        );
    }

    private Map<String, Object> userInput(SkinImageFile image) {
        return Map.of(
                "role", "user",
                "content", List.of(
                        Map.of(
                                "type", "input_text",
                                "text", "Analyze whether this selfie is usable for cosmetic skin condition reference. Return only the required JSON schema."
                        ),
                        Map.of(
                                "type", "input_image",
                                "image_url", dataUrl(image)
                        )
                )
        );
    }

    private String dataUrl(SkinImageFile image) {
        String contentType = image.contentType() == null ? MediaType.IMAGE_JPEG_VALUE : image.contentType();
        return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(image.bytes());
    }

    private String instructions() {
        return """
                You analyze a single selfie image for a cosmetic skin condition reference system.
                This is not medical diagnosis.
                Do not identify diseases or provide treatment advice.
                Return conservative visual signals only.
                
                Quality policy:
                - Set usable=false only when analysis is impossible: no visible face, multiple faces, extreme blur, extreme low light, or face is too small/cropped.
                - For normal quality issues such as mild low light, slight non-frontal angle, suspected makeup, or partial occlusion, keep usable=true and lower confidence.
                - Do not reject only because lighting is not perfect.
                
                Signal policy:
                - blemish: visible blemish/trouble-prone signs.
                - pigmentationTone: visible tone unevenness, spots, pigmentation-like cosmetic tone signs.
                - agingSigns: visible wrinkle/elasticity-related surface signs.
                - drynessHint: visible flaking, roughness, dullness, or dehydration-like surface texture only.
                - oilinessHint: visible shine or sebum-like appearance only. Avoid confusing lighting reflection with oil.
                - rednessHint: visible redness or irritation-like appearance only.
                
                Scoring guidance:
                
                blemish
                - 0-10: almost no visible blemish signal.
                - 11-30: a few minor blemishes or faint post-blemish marks.
                - 31-60: multiple visible blemishes or noticeable trouble-prone appearance.
                - 61-100: widespread or clearly noticeable blemish signals.
                
                pigmentationTone
                - 0-10: relatively even skin tone.
                - 11-30: slight tone unevenness or a few visible pigmentation-like spots.
                - 31-60: noticeable uneven tone or multiple pigmentation-like signals.
                - 61-100: strong visible tone unevenness or widespread pigmentation-like appearance.
                
                agingSigns
                - 0-10: little visible wrinkle or elasticity-related signal.
                - 11-30: mild fine-line or texture-related signal.
                - 31-60: noticeable wrinkle or elasticity-related signal.
                - 61-100: strong visible aging-related signal.
                
                drynessHint
                - 0-10: skin appears generally hydrated and smooth.
                - 11-30: slight roughness or mild dryness signal.
                - 31-60: visible roughness, dullness, or flaky texture.
                - 61-100: strong visible dryness-related texture signal.
                
                oilinessHint
                - 0-10: little visible shine.
                - 11-30: mild shine, usually limited to small areas.
                - 31-60: noticeable shine in the T-zone or multiple facial areas.
                - 61-100: strong sebum-like shine across large facial areas.
                
                rednessHint
                - 0-10: little visible redness.
                - 11-30: mild redness signal.
                - 31-60: noticeable redness signal.
                - 61-100: strong widespread redness signal.
                
                Confidence policy:
                - HIGH: visual evidence is clear and image quality is sufficient.
                - MEDIUM: visual evidence exists but some uncertainty remains.
                - LOW: image quality, lighting, makeup, angle, or occlusion significantly limits judgment.
                
                General rules:
                - Scores represent cosmetic visual signals only.
                - Do not infer hidden skin conditions.
                - Do not use age, gender, ethnicity, or attractiveness as evidence.
                - Avoid extreme scores unless the visual signal is clearly visible.
                - Base all judgments only on what is visible in the image.
                """.trim();
    }

    private String extractOutputText(OpenAiSkinImageAnalysisResponse response) {
        log.info("OpenAI response={}", response);

        if (response == null || response.output() == null) {
            throw new SkinAnalysisAdapterException(SkinAnalysisAdapterErrorCode.IMAGE_ANALYSIS_RESPONSE_INVALID);
        }

        for (OpenAiSkinImageAnalysisResponse.Output output : response.output()) {
            if (output.content() == null) {
                continue;
            }
            for (OpenAiSkinImageAnalysisResponse.Content content : output.content()) {
                if (REFUSAL.equals(content.type())) {
                    throw new SkinAnalysisAdapterException(SkinAnalysisAdapterErrorCode.IMAGE_ANALYSIS_RESPONSE_INVALID);
                }
                if (OUTPUT_TEXT.equals(content.type()) && content.text() != null && !content.text().isBlank()) {
                    return content.text();
                }
            }
        }

        throw new SkinAnalysisAdapterException(SkinAnalysisAdapterErrorCode.IMAGE_ANALYSIS_RESPONSE_INVALID);
    }

    private OpenAiSkinImageAnalysisDraft parseDraft(String outputText) {
        try {
            return objectMapper.readValue(outputText, OpenAiSkinImageAnalysisDraft.class);
        } catch (JsonProcessingException e) {
            log.warn("OpenAI skin image analysis response parse failed.");
            throw new SkinAnalysisAdapterException(SkinAnalysisAdapterErrorCode.IMAGE_ANALYSIS_RESPONSE_INVALID);
        }
    }
}
