package com.pium.config;

import com.pium.adapter.outbound.auth.TossAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TossAuthProperties.class)
public class AuthPropertiesConfig {
}
