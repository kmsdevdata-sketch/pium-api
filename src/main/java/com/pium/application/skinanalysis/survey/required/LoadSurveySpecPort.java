package com.pium.application.skinanalysis.survey.required;

import com.pium.application.skinanalysis.survey.provided.dto.SurveySpecView;

import java.util.Optional;

/**
 * Required Port
 * - 애플리케이션이 현재 활성 설문 스펙을 로드하기 위해 호출하는 포트
 */
public interface LoadSurveySpecPort {

    Optional<SurveySpecView> loadCurrent();
}
