package com.pium.adapter.inbound.web.skinanalysis.survey;

import com.pium.adapter.inbound.response.ApiResponse;
import com.pium.application.skinanalysis.survey.provided.AnalyzeSkinAnalysis;
import com.pium.application.skinanalysis.survey.provided.GetSurveySpec;
import com.pium.application.skinanalysis.survey.provided.dto.AnalyzeResultView;
import com.pium.application.skinanalysis.survey.provided.dto.SurveySpecView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/surveys")
public class SurveyController {

    private final GetSurveySpec getSurveySpec;
    private final AnalyzeSkinAnalysis analyzeSkinAnalysis;

    /**
     * 설문 조회 API
     */
    @GetMapping
    public ApiResponse<SurveySpecResponse> getSurveySpec() {
        SurveySpecView response = getSurveySpec.getSurveySpec();
        return ApiResponse.ok(SurveySpecResponse.from(response));
    }

    /**
     * 설문 분석 API
     */
    @GetMapping("/analyze")
    public ApiResponse<AnalyzeSurveyResponse> analyze(
            @RequestBody AnalyzeSurveyRequest request
    ) {
        AnalyzeResultView response = analyzeSkinAnalysis.analyze(request.toCommand());
        return ApiResponse.ok(AnalyzeSurveyResponse.from(response));
    }
}
