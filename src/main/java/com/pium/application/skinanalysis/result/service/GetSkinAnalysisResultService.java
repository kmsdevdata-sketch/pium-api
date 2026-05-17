package com.pium.application.skinanalysis.result.service;

import com.pium.application.skinanalysis.exception.SurveyApplicationErrorCode;
import com.pium.application.skinanalysis.exception.SurveyApplicationException;
import com.pium.application.skinanalysis.result.dto.SkinAnalysisResultView;
import com.pium.application.skinanalysis.result.provided.GetSkinAnalysisResult;
import com.pium.application.skinanalysis.result.required.LoadSkinAnalysisResultPort;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import com.pium.domain.skinanalysis.vo.SkinAnalysisResultId;
import com.pium.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 피부 분석 결과 조회 유즈케이스 실행
 */
@Service
@RequiredArgsConstructor
public class GetSkinAnalysisResultService implements GetSkinAnalysisResult {

    private final LoadSkinAnalysisResultPort loadSkinAnalysisResultPort;
    private final SkinAnalysisResultViewComposer skinAnalysisResultViewComposer;

    @Override
    public SkinAnalysisResultView getLatest(UserId userId) {
        SkinAnalysisResult result = loadSkinAnalysisResultPort.loadLatest(userId)
                .orElseThrow(() -> new SurveyApplicationException(SurveyApplicationErrorCode.SKIN_ANALYSIS_RESULT_NOT_FOUND));
        return skinAnalysisResultViewComposer.compose(result);
    }

    @Override
    public SkinAnalysisResultView get(UserId userId, SkinAnalysisResultId resultId) {
        SkinAnalysisResult result = loadSkinAnalysisResultPort.load(userId, resultId)
                .orElseThrow(() -> new SurveyApplicationException(SurveyApplicationErrorCode.SKIN_ANALYSIS_RESULT_NOT_FOUND));
        return skinAnalysisResultViewComposer.compose(result);
    }
}
