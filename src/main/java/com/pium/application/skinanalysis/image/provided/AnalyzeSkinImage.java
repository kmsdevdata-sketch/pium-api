package com.pium.application.skinanalysis.image.provided;

import com.pium.application.skinanalysis.image.dto.AnalyzeImageCommand;
import com.pium.application.skinanalysis.image.dto.AnalyzeImageResultView;

public interface AnalyzeSkinImage {

    AnalyzeImageResultView analyze(AnalyzeImageCommand command);
}
