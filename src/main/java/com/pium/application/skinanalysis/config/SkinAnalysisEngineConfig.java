package com.pium.application.skinanalysis.config;

import com.pium.domain.skinanalysis.SkinAnalysisEngine;
import com.pium.domain.skinanalysis.engine.DefaultSkinAnalysisEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SkinAnalysisEngineConfig {

    @Bean
    public SkinAnalysisEngine skinAnalysisEngine() {
        return new DefaultSkinAnalysisEngine();
    }
}
