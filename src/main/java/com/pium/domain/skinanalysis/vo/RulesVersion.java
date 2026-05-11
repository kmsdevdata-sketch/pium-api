package com.pium.domain.skinanalysis.vo;

import com.pium.domain.skinanalysis.exception.SkinAnalysisErrorCode;
import com.pium.domain.skinanalysis.exception.SkinAnalysisException;

public record RulesVersion(String value) {

    public RulesVersion {
        if (value == null || value.isBlank()) {
            throw new SkinAnalysisException(SkinAnalysisErrorCode.INVALID_RULES_VERSION);
        }
    }

    public static RulesVersion of(String value) {
        return new RulesVersion(value == null ? null : value.trim());
    }
}
