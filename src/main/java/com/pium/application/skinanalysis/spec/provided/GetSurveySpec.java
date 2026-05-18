package com.pium.application.skinanalysis.spec.provided;

import com.pium.application.skinanalysis.spec.dto.SurveySpecView;

/**
 * Provided Port
 * - 설문 스펙 조회 유즈케이스
 */
public interface GetSurveySpec {

    /**
     * 현재 활성 스펙 조회
     * @return 프론트 랜더링용 설문 스펙
     */
    SurveySpecView getSurveySpec();
}
