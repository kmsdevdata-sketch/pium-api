package com.pium.domain.skinanalysis.vo;

import com.pium.domain.skinanalysis.enumtype.IngredientGroup;
import com.pium.domain.skinanalysis.exception.SkinAnalysisErrorCode;
import com.pium.domain.skinanalysis.exception.SkinAnalysisException;

public record RequiredIngredient(IngredientGroup group, int weight) {

    private static final int MIN_WEIGHT = 0;
    private static final int MAX_WEIGHT = 100;

    public RequiredIngredient {
        if (group == null) {
            throw new SkinAnalysisException(SkinAnalysisErrorCode.INGREDIENT_GROUP_REQUIRED);
        }
        if (weight < MIN_WEIGHT || weight > MAX_WEIGHT) {
            throw new SkinAnalysisException(SkinAnalysisErrorCode.INVALID_REQUIRED_INGREDIENT_WEIGHT_RANGE);
        }
    }

    public static RequiredIngredient of(IngredientGroup group, int weight) {
        return new RequiredIngredient(group, weight);
    }
}
