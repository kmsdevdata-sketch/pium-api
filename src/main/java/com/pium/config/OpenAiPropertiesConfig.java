package com.pium.config;

import com.pium.adapter.outbound.productprofile.openai.OpenAiProductProfileProperties;
import com.pium.adapter.outbound.skinanalysis.image.openai.OpenAiSkinImageAnalysisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        OpenAiProductProfileProperties.class,
        OpenAiSkinImageAnalysisProperties.class
})
public class OpenAiPropertiesConfig {
}
