package com.pium.domain.skinanalysis.engine;

import com.pium.domain.skinanalysis.enumtype.SkinMetric;

import java.util.Map;

import static com.pium.domain.skinanalysis.enumtype.SkinMetric.*;

public record QuestionRule(
        SkinMetric skinMetric,
        boolean multiSelect,
        Map<String, Integer> optionScores
) {
    public static final Map<String, QuestionRule> QUESTION_RULES = Map.ofEntries(
            // Q_DRYNESS_1: 세안 후 당김 강도 (주 신호)
            // 4단계 선택지는 절대 점수보다 상대 위치 표현을 우선한다.
            // 경계값에 빨리 닿지 않도록 20/40/60/80 구조를 사용한다.
            Map.entry("Q_DRYNESS_1", new QuestionRule(DRYNESS, false, Map.of(
                    "Q1_1", 20,
                    "Q1_2", 40,
                    "Q1_3", 60,
                    "Q1_4", 80
            ))),

            // Q_DRYNESS_2: 건조 발생 시점 패턴 (보정 신호)
            // 5단계 선택지는 패턴 차이를 표현하되 최고점을 85로 제한한다.
            // 세안직후는 장벽 취약 신호, 자기전/하루종일은 내인성 건조 신호로 해석한다.
            Map.entry("Q_DRYNESS_2", new QuestionRule(DRYNESS, false, Map.of(
                    "Q2_1", 10,   // 세안직후
                    "Q2_2", 30,   // 외출후
                    "Q2_3", 55,   // 자기전까지
                    "Q2_4", 85,   // 하루종일 → 만성 건조
                    "Q2_5", 10    // 거의 없음
            ))),

            // Q_OILINESS_1: 오후 유분 분포 상태 (주 신호)
            // 4단계 선택지는 상대 강도만 표현하고 경계 여유를 남긴다.
            Map.entry("Q_OILINESS_1", new QuestionRule(OILINESS, false, Map.of(
                    "Q3_1", 20,   // 여전히 건조 → OILINESS 최저, DRYNESS 보정 트리거
                    "Q3_2", 40,   // 이상 없음 → 중립
                    "Q3_3", 60,   // T존만 번들 → 복합성
                    "Q3_4", 80    // 전체 번들 → 지성
            ))),

            // Q_OILINESS_2: 유분 촉발 요인 (보정 신호, 복수선택)
            // 계산 방식: 선택 항목 점수 합산 후 상한 캡 40
            // Q4_4(느낀 적 없음) 단독 선택 시 → 0점 (예외 처리)
            Map.entry("Q_OILINESS_2", new QuestionRule(OILINESS, true, Map.of(
                    "Q4_1", 15,   // 더위·습도 → 환경성 피지
                    "Q4_2", 15,   // 스트레스 → 호르몬성 피지
                    "Q4_3", 30,   // 항상 번들 → 만성 과분비
                    "Q4_4", 0     // 느낀 적 없음 (단독 선택 시 전체 0 처리)
            ))),

            // Q_BLEMISH_1: 트러블 발생 빈도 (주 신호)
            // 4단계 선택지는 상대 강도 기준으로만 사용한다.
            Map.entry("Q_BLEMISH_1", new QuestionRule(BLEMISH_PRONENESS, false, Map.of(
                    "Q5_1", 20,
                    "Q5_2", 40,
                    "Q5_3", 60,
                    "Q5_4", 80
            ))),

            // Q_BLEMISH_2: 트러블 발생 부위 (보정 신호, 복수선택)
            // 계산 방식: 선택 항목 점수 합산 후 상한 캡 30
            // Q6_6(거의 없음) 단독 선택 시 → 0점
            // 부위별 점수 차등: 전체 선택이 부위 특정보다 높은 보정값
            Map.entry("Q_BLEMISH_2", new QuestionRule(BLEMISH_PRONENESS, true, Map.of(
                    "Q6_1", 8,    // 이마 → 피지 연관
                    "Q6_2", 8,    // 코 주변 → 피지 연관
                    "Q6_3", 10,   // 볼 → BARRIER 교차 신호
                    "Q6_4", 10,   // 턱·입 → 호르몬성
                    "Q6_5", 20,   // 전체 → 만성/광범위
                    "Q6_6", 0     // 거의 없음
            ))),

            // Q_SENSITIVITY_1: 화장품 접촉 반응 (주 신호)
            // 4단계 선택지는 상대 강도 표현용이며, 강한 안전 신호는 후속 단계에서 별도 해석한다.
            Map.entry("Q_SENSITIVITY_1", new QuestionRule(SENSITIVITY, false, Map.of(
                    "Q7_1", 20,
                    "Q7_2", 40,
                    "Q7_3", 60,
                    "Q7_4", 80
            ))),

            // Q_SENSITIVITY_2: 환경 자극 반응 (주 신호 + BARRIER 파생 기여)
            // 4단계 선택지는 상대 강도만 반영하고, 장벽 취약 해석은 후속 파생 단계에서 보강한다.
            Map.entry("Q_SENSITIVITY_2", new QuestionRule(SENSITIVITY, false, Map.of(
                    "Q8_1", 20,
                    "Q8_2", 40,
                    "Q8_3", 60,
                    "Q8_4", 80
            ))),

            // Q_PIGMENTATION_1: 색소침착·톤 고민 강도 (직접 신호)
            // 4단계 선택지는 상대 강도 기준으로 통일한다.
            Map.entry("Q_PIGMENTATION_1", new QuestionRule(PIGMENTATION_TONE, false, Map.of(
                    "Q9_1", 20,
                    "Q9_2", 40,
                    "Q9_3", 60,
                    "Q9_4", 80
            ))),

            // Q_AGING_1: 노화 징후 자가인식 (직접 신호)
            // PIGMENTATION과 동일한 4단계 상대 강도 구조를 사용한다.
            Map.entry("Q_AGING_1", new QuestionRule(AGING_SIGNS, false, Map.of(
                    "Q10_1", 20,
                    "Q10_2", 40,
                    "Q10_3", 60,
                    "Q10_4", 80
            )))
    );

    public static QuestionRule get(String questionId) {
        return QUESTION_RULES.get(questionId);
    }
}
