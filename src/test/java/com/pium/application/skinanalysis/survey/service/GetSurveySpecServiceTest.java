package com.pium.application.skinanalysis.survey.service;

import com.pium.application.skinanalysis.survey.exception.SurveyApplicationErrorCode;
import com.pium.application.skinanalysis.survey.exception.SurveyApplicationException;
import com.pium.application.skinanalysis.survey.provided.dto.SurveySpecView;
import com.pium.application.skinanalysis.survey.required.LoadSurveySpecPort;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GetSurveySpecServiceTest {

    private final LoadSurveySpecPort loadSurveySpecPort = mock(LoadSurveySpecPort.class);
    private final GetSurveySpecService getSurveySpecService = new GetSurveySpecService(loadSurveySpecPort);

    @Test
    void getSurveySpec_returnsValue_whenPresent() {

        SurveySpecView expected = new SurveySpecView(
                List.of(
                        new SurveySpecView.Question(
                                "Q_DRYNESS_1",
                                "세안 후 얼굴이 당기나요?",
                                List.of(
                                        new SurveySpecView.Option("NONE", "거의 없음"),
                                        new SurveySpecView.Option("ALWAYS", "거의 항상")
                                )
                        )
                )
        );

        // loadCurrent호출시 Optional.of(expected) 반환 지시
        when(loadSurveySpecPort.loadCurrent()).thenReturn(Optional.of(expected));

        SurveySpecView actual = getSurveySpecService.getSurveySpec();

        assertThat(actual).isEqualTo(expected);
        // 메서드 호출이 정확히 1번 호출되었는가?
        verify(loadSurveySpecPort, times(1)).loadCurrent();
        // 해당 서비스가 위에 검증한 호출외에 다른 호출이 안일어났는지
        verifyNoMoreInteractions(loadSurveySpecPort);
    }

    @Test
    void getSurveySpec_throwsException_whenEmpty() {
        when(loadSurveySpecPort.loadCurrent()).thenReturn(Optional.empty());

        assertThatThrownBy(getSurveySpecService::getSurveySpec)
                .isInstanceOf(SurveyApplicationException.class)
                .satisfies(ex -> {
                    SurveyApplicationException exception = (SurveyApplicationException) ex;
                    assertThat(exception.getErrorCode()).isEqualTo(SurveyApplicationErrorCode.SURVEY_SPEC_UNAVAILABLE);
                });

        verify(loadSurveySpecPort, times(1)).loadCurrent();
        verifyNoMoreInteractions(loadSurveySpecPort);
    }
}