package com.pium.fixture;

import com.pium.application.skinanalysis.analyze.required.dto.NormalizeSurveySubmission;

import java.util.List;

public class NormalizeSurveySubmissionFixture {

    public static NormalizeSurveySubmission createNormalizeSurveySubmission() {
        return new NormalizeSurveySubmission(
                List.of(new NormalizeSurveySubmission.NormalizedAnswer("Q_DRYNESS_1", List.of("Q1_1"))),
                List.of("Q11_1", "Q11_2")
        );
    }
}
