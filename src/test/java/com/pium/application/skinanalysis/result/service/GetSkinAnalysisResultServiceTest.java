package com.pium.application.skinanalysis.result.service;

import com.pium.application.skinanalysis.exception.SurveyApplicationException;
import com.pium.application.skinanalysis.result.dto.SkinAnalysisResultView;
import com.pium.application.skinanalysis.result.required.LoadSkinAnalysisResultPort;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import com.pium.domain.skinanalysis.vo.SkinAnalysisResultId;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetSkinAnalysisResultServiceTest {

    private final LoadSkinAnalysisResultPort loadSkinAnalysisResultPort = mock(LoadSkinAnalysisResultPort.class);
    private final SkinAnalysisResultViewComposer skinAnalysisResultViewComposer = new SkinAnalysisResultViewComposer();
    private final GetSkinAnalysisResultService service = new GetSkinAnalysisResultService(
            loadSkinAnalysisResultPort,
            skinAnalysisResultViewComposer
    );

    @Test
    void getLatest_안전게이트가_있으면_HOME기준_텍스트를_조합한다() {
        UserId userId = UserId.of("user-test-001");
        SkinAnalysisResult result = SkinAnalysisResult.reconstitute(
                SkinAnalysisResultId.of("result-001"),
                userId,
                List.of(
                        SkinMetricScore.of(SkinMetric.DRYNESS, 72),
                        SkinMetricScore.of(SkinMetric.BARRIER, 84),
                        SkinMetricScore.of(SkinMetric.OILINESS, 34),
                        SkinMetricScore.of(SkinMetric.BLEMISH_PRONENESS, 41),
                        SkinMetricScore.of(SkinMetric.SENSITIVITY, 79),
                        SkinMetricScore.of(SkinMetric.PIGMENTATION_TONE, 29),
                        SkinMetricScore.of(SkinMetric.AGING_SIGNS, 37)
                ),
                List.of("Q11_1"),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        when(loadSkinAnalysisResultPort.loadLatest(userId)).thenReturn(Optional.of(result));

        SkinAnalysisResultView view = service.getLatest(userId);

        assertThat(view.oneLiner()).isEqualTo("장벽과 민감도 신호가 함께 높아요. 지금 피부는 쉬어야 할 타이밍이에요.");
        assertThat(view.summary()).contains("장벽이 약해지면서 자극에도 민감해진 상태");
        assertThat(view.categoryDetails()).hasSize(7);
        assertThat(view.categoryDetails().get(1).metricKey()).isEqualTo("BARRIER");
        assertThat(view.categoryDetails().get(1).level()).isEqualTo("HIGH");
        verify(loadSkinAnalysisResultPort).loadLatest(userId);
    }

    @Test
    void get_정의되지_않은_조합이면_상위_HIGH_단일축으로_fallback한다() {
        UserId userId = UserId.of("user-test-002");
        SkinAnalysisResultId resultId = SkinAnalysisResultId.of("result-002");
        SkinAnalysisResult result = SkinAnalysisResult.reconstitute(
                resultId,
                userId,
                List.of(
                        SkinMetricScore.of(SkinMetric.DRYNESS, 31),
                        SkinMetricScore.of(SkinMetric.BARRIER, 28),
                        SkinMetricScore.of(SkinMetric.OILINESS, 74),
                        SkinMetricScore.of(SkinMetric.BLEMISH_PRONENESS, 29),
                        SkinMetricScore.of(SkinMetric.SENSITIVITY, 45),
                        SkinMetricScore.of(SkinMetric.PIGMENTATION_TONE, 27),
                        SkinMetricScore.of(SkinMetric.AGING_SIGNS, 76)
                ),
                List.of("Q11_2"),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
        );

        when(loadSkinAnalysisResultPort.load(userId, resultId)).thenReturn(Optional.of(result));

        SkinAnalysisResultView view = service.get(userId, resultId);

        assertThat(view.oneLiner()).isEqualTo("피지 분비가 활발한 상태예요. 억제보다 균형 조절이 장기적으로 더 효과적이에요.");
        assertThat(view.summary()).contains("전반적인 피지 과다 신호가 뚜렷해요.");
        verify(loadSkinAnalysisResultPort).load(userId, resultId);
    }

    @Test
    void getLatest_결과가_없으면_예외를_던진다() {
        UserId userId = UserId.of("user-test-003");
        when(loadSkinAnalysisResultPort.loadLatest(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLatest(userId))
                .isInstanceOf(SurveyApplicationException.class);
    }
}
