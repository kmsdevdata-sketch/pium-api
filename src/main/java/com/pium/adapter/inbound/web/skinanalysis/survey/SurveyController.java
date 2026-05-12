package com.pium.adapter.inbound.web.skinanalysis.survey;

import com.pium.adapter.inbound.response.ApiResponse;
import com.pium.application.skinanalysis.survey.provided.GetSurveySpec;
import com.pium.application.skinanalysis.survey.provided.dto.SurveySpecView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/surveys")
public class SurveyController {

    private final GetSurveySpec getSurveySpec;

    @GetMapping
    public ApiResponse<SurveySpecResponse> getSurveySpec() {
        SurveySpecView view = getSurveySpec.getSurveySpec();
        return ApiResponse.ok(SurveySpecResponse.from(view));
    }
}
