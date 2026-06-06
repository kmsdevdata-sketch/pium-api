package com.pium.adapter.outbound.skinanalysis.image.openai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai.skin-image-analysis")
public record OpenAiSkinImageAnalysisProperties(
        String apiKey,
        String responsesUri,
        String model
) {
}
