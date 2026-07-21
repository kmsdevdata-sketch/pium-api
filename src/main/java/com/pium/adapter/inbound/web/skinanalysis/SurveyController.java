package com.pium.adapter.inbound.web.skinanalysis;

import com.pium.adapter.inbound.web.auth.AuthenticatedUserIdResolver;
import com.pium.adapter.inbound.response.ApiResponse;
import com.pium.adapter.inbound.web.skinanalysis.analyze.AnalyzeSurveyRequest;
import com.pium.adapter.inbound.web.skinanalysis.analyze.AnalyzeSurveyResponse;
import com.pium.adapter.inbound.web.skinanalysis.spec.SurveySpecResponse;
import com.pium.application.skinanalysis.analyze.provided.AnalyzeSkinAnalysis;
import com.pium.application.skinanalysis.spec.provided.GetSurveySpec;
import com.pium.application.skinanalysis.analyze.dto.AnalyzeResultView;
import com.pium.application.skinanalysis.spec.dto.SurveySpecView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/surveys")
public class SurveyController {

    private final GetSurveySpec getSurveySpec;
    private final AnalyzeSkinAnalysis analyzeSkinAnalysis;
    private final AuthenticatedUserIdResolver authenticatedUserIdResolver;

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
    @PostMapping("/analyze")
    public ApiResponse<AnalyzeSurveyResponse> analyze(
            @Valid @RequestBody AnalyzeSurveyRequest request,
            Authentication authentication
    ) {
        AnalyzeResultView response = analyzeSkinAnalysis.analyze(
                request.toCommand(authenticatedUserIdResolver.resolve(authentication))
        );
        return ApiResponse.ok(AnalyzeSurveyResponse.from(response));
    }
}
