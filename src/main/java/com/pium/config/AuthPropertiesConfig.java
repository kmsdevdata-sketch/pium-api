package com.pium.config;

import com.pium.adapter.outbound.auth.jwt.JwtProperties;
import com.pium.adapter.outbound.auth.toss.TossAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        TossAuthProperties.class,
        JwtProperties.class
})
public class AuthPropertiesConfig {
}
