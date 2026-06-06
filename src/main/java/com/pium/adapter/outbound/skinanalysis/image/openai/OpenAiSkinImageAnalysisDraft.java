package com.pium.adapter.outbound.skinanalysis.image.openai;

import com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis;

import java.util.List;

record OpenAiSkinImageAnalysisDraft(
        ImageQualityDraft imageQuality,
        VisualSignalsDraft visualSignals,
        List<String> warnings
) {

    ImageSkinAnalysis toApplicationDto() {
        return new ImageSkinAnalysis(
                imageQuality.toApplicationDto(),
                visualSignals.toApplicationDto(),
                warnings
        );
    }

    record ImageQualityDraft(
            boolean usable,
            List<String> reasonCodes
    ) {

        ImageSkinAnalysis.ImageQuality toApplicationDto() {
            return new ImageSkinAnalysis.ImageQuality(usable, reasonCodes);
        }
    }

    record VisualSignalsDraft(
            VisualSignalDraft blemish,
            VisualSignalDraft pigmentationTone,
            VisualSignalDraft agingSigns,
            VisualSignalDraft drynessHint,
            VisualSignalDraft oilinessHint,
            VisualSignalDraft rednessHint
    ) {

        ImageSkinAnalysis.VisualSignals toApplicationDto() {
            return new ImageSkinAnalysis.VisualSignals(
                    blemish.toApplicationDto(),
                    pigmentationTone.toApplicationDto(),
                    agingSigns.toApplicationDto(),
                    drynessHint.toApplicationDto(),
                    oilinessHint.toApplicationDto(),
                    rednessHint.toApplicationDto()
            );
        }
    }

    record VisualSignalDraft(
            int score,
            ImageSkinAnalysis.Confidence confidence
    ) {

        ImageSkinAnalysis.VisualSignal toApplicationDto() {
            return new ImageSkinAnalysis.VisualSignal(score, confidence);
        }
    }
}
