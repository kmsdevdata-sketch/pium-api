package com.pium.application.skinanalysis.result.provided;

import com.pium.application.skinanalysis.result.dto.SkinAnalysisResultView;
import com.pium.domain.skinanalysis.vo.SkinAnalysisResultId;
import com.pium.domain.user.vo.UserId;

/**
 * 피부 분석 결과 조회 유즈케이스
 */
public interface GetSkinAnalysisResult {

    SkinAnalysisResultView getLatest(UserId userId);

    SkinAnalysisResultView get(UserId userId, SkinAnalysisResultId resultId);
}
