package com.pium.application.skinanalysis.image.provided;

import com.pium.application.skinanalysis.image.dto.PreAnalyzeImageCommand;
import com.pium.application.skinanalysis.image.dto.PreAnalyzeImageView;

public interface PreAnalyzeSkinImage {

    PreAnalyzeImageView preAnalyze(PreAnalyzeImageCommand command);
}
