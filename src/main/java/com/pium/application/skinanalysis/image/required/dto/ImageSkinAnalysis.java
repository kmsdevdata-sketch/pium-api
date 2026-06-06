package com.pium.application.skinanalysis.image.required.dto;

import java.util.List;

public record ImageSkinAnalysis(
        ImageQuality imageQuality,
        VisualSignals visualSignals,
        List<String> warnings
) {

    public ImageSkinAnalysis {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public record ImageQuality(
            boolean usable,
            List<String> reasonCodes
    ) {

        public ImageQuality {
            reasonCodes = reasonCodes == null ? List.of() : List.copyOf(reasonCodes);
        }
    }

    public record VisualSignals(
            VisualSignal blemish,
            VisualSignal pigmentationTone,
            VisualSignal agingSigns,
            VisualSignal drynessHint,
            VisualSignal oilinessHint,
            VisualSignal rednessHint
    ) {
    }

    public record VisualSignal(
            int score,
            Confidence confidence
    ) {
    }

    public enum Confidence {
        LOW,
        MEDIUM,
        HIGH
    }
}
