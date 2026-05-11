package com.pium.application.skinanalysis.survey.service;

import com.pium.application.skinanalysis.survey.provided.GetSurveySpec;
import com.pium.application.skinanalysis.survey.provided.dto.SurveySpecView;
import com.pium.application.skinanalysis.survey.required.LoadSurveySpecPort;
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
     * @return
     */
    @Override
    public SurveySpecView getSurveySpec() {
        return loadSurveySpecPort.loadCurrent();
    }
}
