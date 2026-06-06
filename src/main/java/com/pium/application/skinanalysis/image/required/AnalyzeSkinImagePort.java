package com.pium.application.skinanalysis.image.required;

import com.pium.application.skinanalysis.image.dto.SkinImageFile;
import com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis;

public interface AnalyzeSkinImagePort {

    ImageSkinAnalysis analyze(SkinImageFile image);
}
