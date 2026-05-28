package com.pium.adapter.outbound.productprofile.openai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAI ProductProfile 생성 설정.
 */
@ConfigurationProperties(prefix = "openai.product-profile")
public record OpenAiProductProfileProperties(
        String apiKey,
        String responsesUri,
        String model
) {
}
