package com.pium.domain.skinanalysis.vo;

import com.pium.domain.skinanalysis.exception.SkinAnalysisErrorCode;
import com.pium.domain.skinanalysis.exception.SkinAnalysisException;

import java.util.UUID;

public record SkinAnalysisResultId(String value) {

    public SkinAnalysisResultId {
        if (value == null || value.isBlank()) {
            throw new SkinAnalysisException(SkinAnalysisErrorCode.INVALID_SKIN_ANALYSIS_RESULT_ID);
        }
    }

    public static SkinAnalysisResultId newId() {
        return new SkinAnalysisResultId(UUID.randomUUID().toString());
    }

    public static SkinAnalysisResultId of(String value) {
        return new SkinAnalysisResultId(value == null ? null : value.trim());
    }
}
