package com.pium.application.user.home.service;

import com.pium.application.auth.required.LoadUserProfilePort;
import com.pium.application.skinanalysis.result.required.LoadSkinAnalysisResultPort;
import com.pium.application.skinanalysis.result.service.SkinAnalysisResultViewComposer;
import com.pium.application.user.home.dto.UserHomeView;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import com.pium.domain.skinanalysis.vo.SkinAnalysisResultId;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;
import com.pium.domain.user.model.UserProfile;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetUserHomeServiceTest {

    private final LoadUserProfilePort loadUserProfilePort = mock(LoadUserProfilePort.class);
    private final LoadSkinAnalysisResultPort loadSkinAnalysisResultPort = mock(LoadSkinAnalysisResultPort.class);
    private final SkinAnalysisResultViewComposer skinAnalysisResultViewComposer = new SkinAnalysisResultViewComposer();
    private final GetUserHomeService service = new GetUserHomeService(
            loadUserProfilePort,
            loadSkinAnalysisResultPort,
            skinAnalysisResultViewComposer
    );

    @Test
    void getUserHome_최신진단이_있으면_preview를_조합한다() {
        UserId userId = UserId.of("user-test-001");
        SkinAnalysisResult result = SkinAnalysisResult.reconstitute(
                SkinAnalysisResultId.of("result-001"),
                userId,
                List.of(
                        SkinMetricScore.of(SkinMetric.DRYNESS, 82),
                        SkinMetricScore.of(SkinMetric.BARRIER, 65),
                        SkinMetricScore.of(SkinMetric.OILINESS, 58),
                        SkinMetricScore.of(SkinMetric.BLEMISH_PRONENESS, 40),
                        SkinMetricScore.of(SkinMetric.SENSITIVITY, 28),
                        SkinMetricScore.of(SkinMetric.PIGMENTATION_TONE, 27),
                        SkinMetricScore.of(SkinMetric.AGING_SIGNS, 33)
                ),
                List.of("Q11_1"),
                LocalDateTime.of(2026, 5, 17, 15, 12, 41),
                LocalDateTime.of(2026, 5, 17, 15, 12, 41)
        );

        when(loadUserProfilePort.findByUserId(userId))
                .thenReturn(Optional.of(UserProfile.create(userId, "피움닉네임", null)));
        when(loadSkinAnalysisResultPort.countByUserId(userId)).thenReturn(12L);
        when(loadSkinAnalysisResultPort.loadLatest(userId)).thenReturn(Optional.of(result));

        UserHomeView view = service.getUserHome(userId);

        assertThat(view.userName()).isEqualTo("피움닉네임");
        assertThat(view.historyCount()).isEqualTo(12L);
        assertThat(view.latestDiagnosis()).isNotNull();
        assertThat(view.latestDiagnosis().id()).isEqualTo("result-001");
        assertThat(view.latestDiagnosis().createdAt()).isEqualTo(LocalDateTime.of(2026, 5, 17, 15, 12, 41));
        assertThat(view.latestDiagnosis().summary()).contains("건조");
        assertThat(view.latestDiagnosis().previewMetrics()).hasSize(3);
        assertThat(view.latestDiagnosis().previewMetrics().get(0).key()).isEqualTo("DRYNESS");
        assertThat(view.latestDiagnosis().previewMetrics().get(0).level()).isEqualTo("HIGH");
        assertThat(view.latestDiagnosis().previewMetrics().get(1).key()).isEqualTo("BARRIER");
        assertThat(view.latestDiagnosis().previewMetrics().get(1).level()).isEqualTo("MEDIUM");
        verify(loadUserProfilePort).findByUserId(userId);
        verify(loadSkinAnalysisResultPort).countByUserId(userId);
        verify(loadSkinAnalysisResultPort).loadLatest(userId);
    }

    @Test
    void getUserHome_최신진단이_없으면_null을_반환한다() {
        UserId userId = UserId.of("user-test-002");
        when(loadUserProfilePort.findByUserId(userId))
                .thenReturn(Optional.of(UserProfile.create(userId, "피움닉네임", null)));
        when(loadSkinAnalysisResultPort.countByUserId(userId)).thenReturn(0L);
        when(loadSkinAnalysisResultPort.loadLatest(userId)).thenReturn(Optional.empty());

        UserHomeView view = service.getUserHome(userId);

        assertThat(view.userName()).isEqualTo("피움닉네임");
        assertThat(view.historyCount()).isZero();
        assertThat(view.latestDiagnosis()).isNull();
        verify(loadUserProfilePort).findByUserId(userId);
    }
}
