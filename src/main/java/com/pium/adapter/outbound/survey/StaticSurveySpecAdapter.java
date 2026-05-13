package com.pium.adapter.outbound.survey;

import com.pium.adapter.outbound.survey.exception.SurveyAdapterErrorCode;
import com.pium.adapter.outbound.survey.exception.SurveyAdapterException;
import com.pium.application.skinanalysis.survey.provided.dto.SurveySpecView;
import com.pium.application.skinanalysis.survey.required.LoadSurveySpecPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class StaticSurveySpecAdapter implements LoadSurveySpecPort {

    @Override
    public Optional<SurveySpecView> loadCurrent() {
        try {
            return Optional.of(new SurveySpecView(
                    List.of(
                            question("Q_DRYNESS_1", "세안 후 아무것도 바르지 않으면, 피부가 얼마나 당기나요?", drynessTightnessOptions()),
                            question("Q_DRYNESS_2", "피부가 가장 건조하게 느껴지는 때는 언제예요?", drynessPatternOptions()),
                            question("Q_OILINESS_1", "오후쯤 됐을 때, 피부 상태가 어떤 편이에요?", oilinessDistributionOptions()),
                            question("Q_OILINESS_2", "유분이 특히 많아진다고 느낄 때가 있나요? (복수 선택)", oilinessTriggerOptions()),
                            question("Q_BLEMISH_1", "여드름, 뾰루지 같은 트러블이 얼마나 자주 생겨요?", blemishFrequencyOptions()),
                            question("Q_BLEMISH_2", "트러블이 주로 어디에 생기나요? (복수 선택)", blemishAreaOptions()),
                            question("Q_SENSITIVITY_1", "새 화장품을 쓸 때, 따가움·붉어짐·가려움 같은 반응이 생기는 편인가요?", sensitivityProductReactionOptions()),
                            question("Q_SENSITIVITY_2", "건조한 날씨, 바람, 추위 같은 환경 변화에 피부가 민감하게 반응하나요?", sensitivityEnvironmentReactionOptions()),
                            question("Q_PIGMENTATION_1", "잡티, 색소침착, 피부 톤 불균일이 신경 쓰이나요?", pigmentationConcernOptions()),
                            question("Q_AGING_1", "잔주름, 탄력 저하, 피부 처짐이 느껴지나요?", agingConcernOptions()),
                            question("Q_GOAL_1", "지금 피부에서 가장 바꾸고 싶은 게 어떤건가요? (최대 2개)", goalOptions())
                    )
            ));
        } catch (Exception e) {
            throw new SurveyAdapterException(SurveyAdapterErrorCode.SURVEY_SPEC_LOAD_FAILED);
        }
    }

    private static SurveySpecView.Question question(
            String questionId,
            String title,
            List<SurveySpecView.Option> options
    ) {
        return new SurveySpecView.Question(questionId, title, options);
    }

    private static List<SurveySpecView.Option> drynessTightnessOptions() {
        return List.of(
                new SurveySpecView.Option("Q1_1", "거의 안 당겨요"),
                new SurveySpecView.Option("Q1_2", "약간 당기는 느낌이에요"),
                new SurveySpecView.Option("Q1_3", "꽤 당기고 불편해요"),
                new SurveySpecView.Option("Q1_4", "너무 당겨서 표정 짓기도 힘들어요")
        );
    }

    private static List<SurveySpecView.Option> drynessPatternOptions() {
        return List.of(
                new SurveySpecView.Option("Q2_1", "세안 직후"),
                new SurveySpecView.Option("Q2_2", "외출하고 돌아왔을 때"),
                new SurveySpecView.Option("Q2_3", "자기 직전에도 건조해요"),
                new SurveySpecView.Option("Q2_4", "하루 종일 비슷하게 건조해요"),
                new SurveySpecView.Option("Q2_5", "건조한 느낌이 거의 없어요")
        );
    }

    private static List<SurveySpecView.Option> oilinessDistributionOptions() {
        return List.of(
                new SurveySpecView.Option("Q3_1", "여전히 건조하거나 당겨요"),
                new SurveySpecView.Option("Q3_2", "딱히 이상 없어요"),
                new SurveySpecView.Option("Q3_3", "T존(이마·코)만 번들거려요"),
                new SurveySpecView.Option("Q3_4", "볼까지 전체적으로 번들거려요")
        );
    }

    private static List<SurveySpecView.Option> oilinessTriggerOptions() {
        return List.of(
                new SurveySpecView.Option("Q4_1", "덥거나 습한 날"),
                new SurveySpecView.Option("Q4_2", "스트레스받을 때"),
                new SurveySpecView.Option("Q4_3", "특별한 이유 없이 항상 번들거려요"),
                new SurveySpecView.Option("Q4_4", "그런 느낌이 없어요")
        );
    }

    private static List<SurveySpecView.Option> blemishFrequencyOptions() {
        return List.of(
                new SurveySpecView.Option("Q5_1", "거의 안 생겨요"),
                new SurveySpecView.Option("Q5_2", "한 달에 1~2개 정도"),
                new SurveySpecView.Option("Q5_3", "매주 새로 생기는 편이에요"),
                new SurveySpecView.Option("Q5_4", "항상 여러 개 있어요")
        );
    }

    private static List<SurveySpecView.Option> blemishAreaOptions() {
        return List.of(
                new SurveySpecView.Option("Q6_1", "이마"),
                new SurveySpecView.Option("Q6_2", "코 주변"),
                new SurveySpecView.Option("Q6_3", "볼"),
                new SurveySpecView.Option("Q6_4", "턱·입 주변"),
                new SurveySpecView.Option("Q6_5", "부위 상관없이 전체적으로"),
                new SurveySpecView.Option("Q6_6", "거의 안 생겨요")
        );
    }

    private static List<SurveySpecView.Option> sensitivityProductReactionOptions() {
        return List.of(
                new SurveySpecView.Option("Q7_1", "거의 없어요"),
                new SurveySpecView.Option("Q7_2", "가끔 있어요 (제품에 따라 달라요)"),
                new SurveySpecView.Option("Q7_3", "자주 있어요 (새 제품엔 늘 조심해요)"),
                new SurveySpecView.Option("Q7_4", "거의 항상 반응이 생겨서 제품 고르기가 힘들어요")
        );
    }

    private static List<SurveySpecView.Option> sensitivityEnvironmentReactionOptions() {
        return List.of(
                new SurveySpecView.Option("Q8_1", "전혀 그렇지 않아요"),
                new SurveySpecView.Option("Q8_2", "약간 그런 편이에요"),
                new SurveySpecView.Option("Q8_3", "꽤 그런 편이에요"),
                new SurveySpecView.Option("Q8_4", "계절 바뀔 때마다 피부 상태가 크게 달라져요")
        );
    }

    private static List<SurveySpecView.Option> pigmentationConcernOptions() {
        return List.of(
                new SurveySpecView.Option("Q9_1", "전혀 신경 안 써요"),
                new SurveySpecView.Option("Q9_2", "약간 신경 쓰여요"),
                new SurveySpecView.Option("Q9_3", "꽤 신경 쓰여요"),
                new SurveySpecView.Option("Q9_4", "지금 제일 큰 고민이에요")
        );
    }

    private static List<SurveySpecView.Option> agingConcernOptions() {
        return List.of(
                new SurveySpecView.Option("Q10_1", "전혀 안 느껴져요"),
                new SurveySpecView.Option("Q10_2", "약간 느껴져요"),
                new SurveySpecView.Option("Q10_3", "꽤 느껴지고 신경 쓰여요"),
                new SurveySpecView.Option("Q10_4", "지금 제일 큰 고민이에요")
        );
    }

    private static List<SurveySpecView.Option> goalOptions() {
        return List.of(
                new SurveySpecView.Option("Q11_1", "보습·수분감"),
                new SurveySpecView.Option("Q11_2", "트러블·여드름"),
                new SurveySpecView.Option("Q11_3", "피부 톤·미백"),
                new SurveySpecView.Option("Q11_4", "모공·피지"),
                new SurveySpecView.Option("Q11_5", "탄력·주름"),
                new SurveySpecView.Option("Q11_6", "민감한 피부 진정·장벽 강화")
        );
    }
}
