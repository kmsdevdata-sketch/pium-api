package com.pium.adapter.outbound.skinanalysis.image.openai;

import java.util.List;
import java.util.Map;

final class OpenAiSkinImageAnalysisSchema {

    private OpenAiSkinImageAnalysisSchema() {
    }

    static Map<String, Object> responseFormat() {
        return Map.of(
                "type", "json_schema",
                "name", "skin_image_analysis",
                "strict", true,
                "schema", schema()
        );
    }

    private static Map<String, Object> schema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of("imageQuality", "visualSignals", "warnings"),
                "properties", Map.of(
                        "imageQuality", imageQuality(),
                        "visualSignals", visualSignals(),
                        "warnings", arrayOf(Map.of("type", "string"))
                )
        );
    }

    private static Map<String, Object> imageQuality() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of("usable", "reasonCodes"),
                "properties", Map.of(
                        "usable", Map.of("type", "boolean"),
                        "reasonCodes", enumArray(List.of(
                                "NONE",
                                "FACE_NOT_VISIBLE",
                                "FACE_TOO_SMALL_OR_CROPPED",
                                "MULTIPLE_FACES",
                                "EXTREME_BLUR",
                                "EXTREME_LOW_LIGHT",
                                "LOW_LIGHT",
                                "BACKLIGHT",
                                "NOT_FRONTAL",
                                "OCCLUDED_FACE",
                                "FILTER_OR_BEAUTY_EFFECT_SUSPECTED",
                                "HEAVY_MAKEUP_SUSPECTED"
                        ))
                )
        );
    }

    private static Map<String, Object> visualSignals() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of(
                        "blemish",
                        "pigmentationTone",
                        "agingSigns",
                        "drynessHint",
                        "oilinessHint",
                        "rednessHint"
                ),
                "properties", Map.of(
                        "blemish", visualSignal(),
                        "pigmentationTone", visualSignal(),
                        "agingSigns", visualSignal(),
                        "drynessHint", visualSignal(),
                        "oilinessHint", visualSignal(),
                        "rednessHint", visualSignal()
                )
        );
    }

    private static Map<String, Object> visualSignal() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of("score", "confidence"),
                "properties", Map.of(
                        "score", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                        "confidence", Map.of("type", "string", "enum", List.of("LOW", "MEDIUM", "HIGH"))
                )
        );
    }

    private static Map<String, Object> enumArray(List<String> values) {
        return arrayOf(Map.of("type", "string", "enum", values));
    }

    private static Map<String, Object> arrayOf(Map<String, Object> items) {
        return Map.of("type", "array", "items", items);
    }
}
