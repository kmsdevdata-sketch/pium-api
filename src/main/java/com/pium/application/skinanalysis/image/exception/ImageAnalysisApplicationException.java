package com.pium.application.skinanalysis.image.exception;

import com.pium.application.exception.ApplicationException;
import com.pium.exception.ErrorCode;

public class ImageAnalysisApplicationException extends ApplicationException {

    public ImageAnalysisApplicationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
