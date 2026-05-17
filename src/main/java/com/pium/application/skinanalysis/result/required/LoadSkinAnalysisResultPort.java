package com.pium.application.skinanalysis.result.required;

import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import com.pium.domain.skinanalysis.vo.SkinAnalysisResultId;
import com.pium.domain.user.vo.UserId;

import java.util.List;
import java.util.Optional;

/**
 * 피부 분석 결과를 조회한다.
 */
public interface LoadSkinAnalysisResultPort {

    long countByUserId(UserId userId);

    List<SkinAnalysisResult> loadAll(UserId userId);

    Optional<SkinAnalysisResult> loadLatest(UserId userId);

    Optional<SkinAnalysisResult> load(UserId userId, SkinAnalysisResultId resultId);
}
