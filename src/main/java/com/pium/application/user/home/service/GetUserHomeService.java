package com.pium.application.user.home.service;

import com.pium.application.auth.required.LoadUserProfilePort;
import com.pium.application.skinanalysis.result.dto.SkinAnalysisResultView;
import com.pium.application.skinanalysis.result.required.LoadSkinAnalysisResultPort;
import com.pium.application.skinanalysis.result.service.SkinAnalysisResultViewComposer;
import com.pium.application.user.home.dto.UserHomeView;
import com.pium.application.user.home.provided.GetUserHome;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import com.pium.domain.user.exception.UserErrorCode;
import com.pium.domain.user.exception.UserException;
import com.pium.domain.user.model.UserProfile;
import com.pium.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * 홈 화면 요약 정보를 조합한다.
 */
@Service
@RequiredArgsConstructor
public class GetUserHomeService implements GetUserHome {

    private final LoadUserProfilePort loadUserProfilePort;
    private final LoadSkinAnalysisResultPort loadSkinAnalysisResultPort;
    private final SkinAnalysisResultViewComposer skinAnalysisResultViewComposer;

    @Override
    public UserHomeView getUserHome(UserId userId) {
        long historyCount = loadSkinAnalysisResultPort.countByUserId(userId);

        String userName = loadUserProfilePort.findByUserId(userId)
                .map(UserProfile::getNickname)
                .orElseThrow(() -> new UserException(UserErrorCode.INVALID_USER_PROFILE_ID));

        return loadSkinAnalysisResultPort.loadLatest(userId)
                .map(this::toLatestDiagnosisView)
                .map(latestDiagnosis -> new UserHomeView(userName,historyCount, latestDiagnosis))
                .orElseGet(() -> new UserHomeView(userName,historyCount, null));
    }

    private UserHomeView.LatestDiagnosisView toLatestDiagnosisView(SkinAnalysisResult result) {
        SkinAnalysisResultView resultView = skinAnalysisResultViewComposer.compose(result);

        List<UserHomeView.ResultMetricPreviewView> previewMetrics = resultView.categoryDetails().stream()
                .sorted(Comparator.comparingInt(SkinAnalysisResultView.CategoryDetailView::score).reversed())
                .limit(3)
                .map(metric -> new UserHomeView.ResultMetricPreviewView(
                        metric.metricKey(),
                        labelOf(metric.metricKey()),
                        metric.score(),
                        homeLevelOf(metric.level())
                ))
                .toList();

        return new UserHomeView.LatestDiagnosisView(
                resultView.resultId(),
                resultView.createdAt(),
                resultView.oneLiner(),
                previewMetrics
        );
    }

    private String labelOf(String metricKey) {
        return switch (metricKey) {
            case "DRYNESS" -> "건조";
            case "BARRIER" -> "장벽";
            case "OILINESS" -> "유분";
            case "BLEMISH_PRONENESS" -> "트러블";
            case "SENSITIVITY" -> "민감";
            case "PIGMENTATION_TONE" -> "색소·톤";
            case "AGING_SIGNS" -> "노화";
            default -> metricKey;
        };
    }

    private String homeLevelOf(String level) {
        return switch (level) {
            case "MID" -> "MEDIUM";
            default -> level;
        };
    }
}
