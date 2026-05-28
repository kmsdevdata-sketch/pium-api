package com.pium.config;

import com.pium.adapter.outbound.productprofile.openai.OpenAiProductProfileProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenAiProductProfileProperties.class)
public class OpenAiPropertiesConfig {
}
