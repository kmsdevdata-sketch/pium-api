package com.pium.adapter.outbound.productprofile.openai;

import java.util.List;
import java.util.Map;

/**
 * ProductProfile Structured Outputs schema.
 */
final class OpenAiProductProfileSchema {

    private OpenAiProductProfileSchema() {
    }

    static Map<String, Object> responseFormat() {
        return Map.of(
                "type", "json_schema",
                "name", "product_profile",
                "strict", true,
                "schema", schema()
        );
    }

    private static Map<String, Object> schema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of(
                        "benefitTraits",
                        "riskTraits",
                        "ingredientGroups",
                        "activeFamilies",
                        "evidenceSignals",
                        "warnings"
                ),
                "properties", Map.of(
                        "benefitTraits", arrayOf(benefitTrait()),
                        "riskTraits", arrayOf(riskTrait()),
                        "ingredientGroups", enumArray("ProductIngredientGroup", List.of(
                                "HUMECTANT",
                                "BARRIER_LIPID",
                                "SOOTHING",
                                "SEBUM_PORE",
                                "BLEMISH_CARE",
                                "BRIGHTENING",
                                "ANTI_AGING",
                                "UV_FILTER",
                                "FRAGRANCE_ALLERGEN",
                                "EXFOLIATING_ACID",
                                "HEAVY_OCCLUSIVE"
                        )),
                        "activeFamilies", enumArray("ActiveFamily", List.of(
                                "CERAMIDE",
                                "PANTHENOL",
                                "NIACINAMIDE",
                                "RETINOID_LIKE",
                                "AHA",
                                "BHA",
                                "PHA",
                                "LHA",
                                "VITAMIN_C",
                                "PEPTIDE",
                                "ADENOSINE",
                                "UV_FILTER",
                                "FRAGRANCE",
                                "HEAVY_OIL_WAX"
                        )),
                        "evidenceSignals", arrayOf(evidenceSignal()),
                        "warnings", arrayOf(Map.of("type", "string"))
                )
        );
    }

    private static Map<String, Object> benefitTrait() {
        return traitSignal(List.of(
                "HYDRATION_SUPPORT",
                "BARRIER_SUPPORT",
                "SOOTHING_SUPPORT",
                "SEBUM_CONTROL_SUPPORT",
                "BLEMISH_CARE_SUPPORT",
                "BRIGHTENING_SUPPORT",
                "ANTI_AGING_SUPPORT",
                "UV_PROTECTION",
                "EXFOLIATION_EFFECT"
        ));
    }

    private static Map<String, Object> riskTrait() {
        return traitSignal(List.of(
                "IRRITATION_RISK",
                "HIGH_IRRITATION_RISK",
                "FRAGRANCE_OR_ALLERGEN_RISK",
                "COMEDOGENIC_RISK",
                "DRYING_OR_STRIPPING_RISK",
                "STRONG_EXFOLIATION_EFFECT",
                "STRONG_ACTIVE_RISK",
                "HEAVY_OCCLUSIVE_RISK"
        ));
    }

    private static Map<String, Object> traitSignal(List<String> traits) {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of("trait", "strength", "confidence", "evidenceRefs"),
                "properties", Map.of(
                        "trait", Map.of("type", "string", "enum", traits),
                        "strength", Map.of("type", "string", "enum", List.of("WEAK", "MODERATE", "STRONG")),
                        "confidence", confidence(),
                        "evidenceRefs", arrayOf(Map.of("type", "string"))
                )
        );
    }

    private static Map<String, Object> evidenceSignal() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of("id", "type", "sourceField", "message", "confidence"),
                "properties", Map.of(
                        "id", Map.of("type", "string"),
                        "type", Map.of("type", "string", "enum", List.of(
                                "REGULATORY_LABEL",
                                "INGREDIENT_PRESENT",
                                "INGREDIENT_POSITION",
                                "INGREDIENT_GROUP",
                                "CATEGORY",
                                "MARKETING_CLAIM",
                                "ADMIN_REVIEW"
                        )),
                        "sourceField", Map.of("type", "string", "enum", List.of(
                                "BRAND_NAME",
                                "PRODUCT_NAME",
                                "CATEGORY",
                                "USAGE_STEP",
                                "FUNCTIONAL_LABELS",
                                "INGREDIENTS",
                                "CLAIMS",
                                "ADMIN_MEMO"
                        )),
                        "message", Map.of("type", "string"),
                        "confidence", confidence()
                )
        );
    }

    private static Map<String, Object> confidence() {
        return Map.of("type", "string", "enum", List.of("LOW", "MEDIUM", "HIGH"));
    }

    private static Map<String, Object> enumArray(String name, List<String> values) {
        return arrayOf(Map.of("type", "string", "description", name, "enum", values));
    }

    private static Map<String, Object> arrayOf(Map<String, Object> items) {
        return Map.of("type", "array", "items", items);
    }
}
