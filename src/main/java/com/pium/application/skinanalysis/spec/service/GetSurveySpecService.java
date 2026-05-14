package com.pium.application.skinanalysis.spec.service;

import com.pium.application.skinanalysis.spec.provided.GetSurveySpec;
import com.pium.application.skinanalysis.spec.dto.SurveySpecView;
import com.pium.application.skinanalysis.spec.required.LoadSurveySpecPort;
import com.pium.application.skinanalysis.exception.SurveyApplicationErrorCode;
import com.pium.application.skinanalysis.exception.SurveyApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Provided Port 구현체
 * - 설문 스펙을 조회 유즈케이스 실행
 */
@Service
@RequiredArgsConstructor
public class GetSurveySpecService implements GetSurveySpec {

    private final LoadSurveySpecPort loadSurveySpecPort;

    /**
     * 현재 활성 스펙 조회
     *
     * @return
     */
    @Override
    public SurveySpecView getSurveySpec() {
        return loadSurveySpecPort.loadCurrent()
                .orElseThrow(() -> new SurveyApplicationException(SurveyApplicationErrorCode.SURVEY_SPEC_UNAVAILABLE));
    }

}
