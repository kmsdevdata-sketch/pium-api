package com.pium.adapter.outbound.skinanalysis.exception;

import com.pium.adapter.exception.AdapterException;
import com.pium.exception.ErrorCode;

public class SkinAnalysisAdapterException extends AdapterException {
    public SkinAnalysisAdapterException(ErrorCode errorCode) {
        super(errorCode);
    }

}
