package com.pium.config;

import com.pium.adapter.outbound.auth.google.GoogleAuthProperties;
import com.pium.adapter.outbound.auth.jwt.JwtProperties;
import com.pium.adapter.outbound.auth.kakao.KakaoAuthProperties;
import com.pium.adapter.outbound.auth.toss.TossAuthProperties;
import com.pium.config.web.AppCorsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        TossAuthProperties.class,
        GoogleAuthProperties.class,
        KakaoAuthProperties.class,
        JwtProperties.class,
        AppCorsProperties.class
})
public class AuthPropertiesConfig {
}
