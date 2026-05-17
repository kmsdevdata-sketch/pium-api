package com.pium.application.skinanalysis.result.service;

import com.pium.application.skinanalysis.result.dto.SkinAnalysisResultView;
import com.pium.domain.skinanalysis.enumtype.SkinMetric;
import com.pium.domain.skinanalysis.model.SkinAnalysisResult;
import com.pium.domain.skinanalysis.vo.SkinMetricScore;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 피부 분석 결과를 프론트 응답용 텍스트로 조합한다.
 */
@Component
public class SkinAnalysisResultViewComposer {

    private static final int MID_MIN_SCORE = 35;
    private static final int HIGH_MIN_SCORE = 70;

    private static final List<SkinMetric> METRIC_ORDER = List.of(
            SkinMetric.DRYNESS,
            SkinMetric.BARRIER,
            SkinMetric.OILINESS,
            SkinMetric.BLEMISH_PRONENESS,
            SkinMetric.SENSITIVITY,
            SkinMetric.PIGMENTATION_TONE,
            SkinMetric.AGING_SIGNS
    );

    private static final List<SkinMetric> HIGH_PRIORITY = List.of(
            SkinMetric.BARRIER,
            SkinMetric.SENSITIVITY,
            SkinMetric.DRYNESS,
            SkinMetric.OILINESS,
            SkinMetric.BLEMISH_PRONENESS,
            SkinMetric.PIGMENTATION_TONE,
            SkinMetric.AGING_SIGNS
    );

    public SkinAnalysisResultView compose(SkinAnalysisResult result) {
        Map<SkinMetric, ScoredMetric> scoredMetricMap = toScoredMetricMap(result.getSkinMetricScores());
        List<ScoredMetric> orderedMetrics = METRIC_ORDER.stream()
                .map(scoredMetricMap::get)
                .toList();

        List<SkinMetric> highMetrics = orderedMetrics.stream()
                .filter(scoredMetric -> scoredMetric.level() == MetricLevel.HIGH)
                .map(ScoredMetric::metric)
                .sorted(Comparator.comparingInt(HIGH_PRIORITY::indexOf))
                .toList();

        List<SkinAnalysisResultView.SkinMetricScoreView> skinMetricScores = orderedMetrics.stream()
                .map(scoredMetric -> new SkinAnalysisResultView.SkinMetricScoreView(
                        scoredMetric.metric().name(),
                        scoredMetric.score(),
                        scoredMetric.level().name()
                ))
                .toList();

        List<SkinAnalysisResultView.CategoryDetailView> categoryDetails = orderedMetrics.stream()
                .map(scoredMetric -> {
                    TextBlock textBlock = categoryText(scoredMetric.metric(), scoredMetric.level());
                    return new SkinAnalysisResultView.CategoryDetailView(
                            scoredMetric.metric().name(),
                            scoredMetric.score(),
                            scoredMetric.level().name(),
                            textBlock.stateText(),
                            textBlock.insight()
                    );
                })
                .toList();

        return new SkinAnalysisResultView(
                result.getId().value(),
                result.getCreatedAt(),
                oneLiner(highMetrics),
                skinMetricScores,
                categoryDetails,
                summary(highMetrics, scoredMetricMap)
        );
    }

    private Map<SkinMetric, ScoredMetric> toScoredMetricMap(List<SkinMetricScore> scores) {
        Map<SkinMetric, ScoredMetric> scoredMetricMap = new EnumMap<>(SkinMetric.class);
        for (SkinMetricScore score : scores) {
            scoredMetricMap.put(
                    score.metric(),
                    new ScoredMetric(score.metric(), score.score(), levelOf(score.score()))
            );
        }
        return scoredMetricMap;
    }

    private MetricLevel levelOf(int score) {
        if (score >= HIGH_MIN_SCORE) {
            return MetricLevel.HIGH;
        }
        if (score >= MID_MIN_SCORE) {
            return MetricLevel.MID;
        }
        return MetricLevel.LOW;
    }

    private String oneLiner(List<SkinMetric> highMetrics) {
        if (highMetrics.contains(SkinMetric.BARRIER) || highMetrics.contains(SkinMetric.SENSITIVITY)) {
            return safetyOneLiner(highMetrics);
        }

        if (highMetrics.size() >= 2) {
            String exact = balancedOneLiner(pairKey(highMetrics.get(0), highMetrics.get(1)));
            if (exact != null) {
                return exact;
            }
        }

        if (!highMetrics.isEmpty()) {
            return singleHighOneLiner(highMetrics.get(0));
        }

        return "전반적으로 안정적인 피부 상태예요. 지금 루틴을 유지하면서 고민 부위를 조금씩 보완해보세요.";
    }

    private String safetyOneLiner(List<SkinMetric> highMetrics) {
        if (highMetrics.contains(SkinMetric.BARRIER) && highMetrics.contains(SkinMetric.SENSITIVITY)) {
            return "장벽과 민감도 신호가 함께 높아요. 지금 피부는 쉬어야 할 타이밍이에요.";
        }

        if (highMetrics.contains(SkinMetric.BARRIER)) {
            SkinMetric secondary = firstSecondary(highMetrics, SkinMetric.BARRIER);
            if (secondary == SkinMetric.DRYNESS) {
                return "건조함과 장벽 약화가 함께 나타나고 있어요. 수분 공급보다 장벽 회복이 먼저예요.";
            }
            if (secondary == SkinMetric.BLEMISH_PRONENESS) {
                return "트러블 신호는 강하지만 장벽이 먼저 안정돼야 해요. 강한 케어는 오히려 역효과일 수 있어요.";
            }
            return "피부 장벽이 예민해진 상태예요. 지금은 채우는 것보다 지키는 게 먼저예요.";
        }

        SkinMetric secondary = firstSecondary(highMetrics, SkinMetric.SENSITIVITY);
        if (secondary == SkinMetric.DRYNESS) {
            return "건조하면서 자극에도 민감한 상태예요. 순하고 밀도 있는 보습이 지금 가장 필요해요.";
        }
        if (secondary == SkinMetric.BLEMISH_PRONENESS) {
            return "트러블 걱정은 크지만 지금 피부의 자극 허용도가 낮아요. 진정이 트러블 케어보다 앞서야 해요.";
        }
        if (secondary == SkinMetric.OILINESS) {
            return "피지는 많지만 피부는 자극에 예민한 상태예요. 강한 피지 케어보다 균형 잡힌 접근이 필요해요.";
        }
        return "피부가 자극에 민감하게 반응하는 시기예요. 성분보다 안정이 우선이에요.";
    }

    private String balancedOneLiner(String pairKey) {
        return switch (pairKey) {
            case "DRYNESS+OILINESS" ->
                    "수분은 부족하고 피지는 많은 복합 상태예요. 유·수분 균형이 지금 피부의 핵심 과제예요.";
            case "DRYNESS+BLEMISH_PRONENESS" ->
                    "건조하면서 트러블도 반복되고 있어요. 보습 부족이 트러블을 악화시키는 패턴일 수 있어요.";
            case "DRYNESS+AGING_SIGNS" ->
                    "건조함과 탄력 저하 신호가 함께 나타나고 있어요. 수분이 탄력의 기반이에요. 보습이 먼저예요.";
            case "OILINESS+BLEMISH_PRONENESS" ->
                    "피지 과다와 트러블 경향이 함께 강하게 나타나고 있어요. 피지 조절이 트러블 개선의 출발점이에요.";
            case "BLEMISH_PRONENESS+PIGMENTATION_TONE" ->
                    "트러블 흔적이 색소로 이어지는 패턴이에요. 트러블 관리가 곧 톤 케어예요.";
            case "PIGMENTATION_TONE+AGING_SIGNS" ->
                    "탄력과 톤 두 가지 고민이 함께 있어요. 항산화와 재생 중심의 루틴이 두 고민을 동시에 다룰 수 있어요.";
            case "DRYNESS+PIGMENTATION_TONE" ->
                    "건조한 피부는 톤이 더 칙칙해 보여요. 보습이 곧 미백의 첫 단계예요.";
            default -> null;
        };
    }

    private String singleHighOneLiner(SkinMetric metric) {
        return switch (metric) {
            case DRYNESS -> "건조 신호가 집중적으로 나타나고 있어요. 수분 공급과 유지 모두 챙겨야 할 타이밍이에요.";
            case OILINESS -> "피지 분비가 활발한 상태예요. 억제보다 균형 조절이 장기적으로 더 효과적이에요.";
            case BLEMISH_PRONENESS -> "트러블이 반복되는 경향이 뚜렷해요. 발생 원인을 파악하는 게 케어의 시작이에요.";
            case SENSITIVITY -> "자극 반응이 자주 나타나는 민감한 상태예요. 새로운 제품보다 검증된 루틴이 지금은 맞아요.";
            case PIGMENTATION_TONE -> "톤 불균일과 색소 고민 신호가 강하게 나타나고 있어요. 자외선 차단이 모든 미백 케어의 전제예요.";
            case AGING_SIGNS -> "탄력과 주름 신호가 뚜렷하게 관측되고 있어요. 지금부터의 루틴이 5년 후 피부를 결정해요.";
            case BARRIER -> "피부 장벽이 약해진 신호가 나타나고 있어요. 지금은 새로운 케어를 추가하는 것보다 기본에 집중할 때예요.";
        };
    }

    private String summary(List<SkinMetric> highMetrics, Map<SkinMetric, ScoredMetric> scoredMetricMap) {
        if (highMetrics.contains(SkinMetric.BARRIER) || highMetrics.contains(SkinMetric.SENSITIVITY)) {
            return safetySummary(highMetrics);
        }

        if (highMetrics.size() >= 2) {
            String exact = balancedSummary(pairKey(highMetrics.get(0), highMetrics.get(1)));
            if (exact != null) {
                return exact;
            }
        }

        if (!highMetrics.isEmpty()) {
            ScoredMetric scoredMetric = scoredMetricMap.get(highMetrics.get(0));
            TextBlock textBlock = categoryText(scoredMetric.metric(), scoredMetric.level());
            return textBlock.stateText() + " " + textBlock.insight();
        }

        return """
                지금 피부는 전반적으로 안정적인 상태예요.
                특별히 집중 케어가 필요한 신호가 높게 나타나지 않았어요.
                현재 루틴을 유지하면서 가장 신경 쓰이는 한 가지 고민에만 집중하는 게 좋아요.
                여러 성분을 한꺼번에 도입하기보다, 하나를 꾸준히 써서 효과를 확인하는 접근이 장기적으로 더 효율적이에요.
                """.trim();
    }

    private String safetySummary(List<SkinMetric> highMetrics) {
        if (highMetrics.contains(SkinMetric.BARRIER) && highMetrics.contains(SkinMetric.SENSITIVITY)) {
            return """
                    지금 피부는 장벽이 약해지면서 자극에도 민감해진 상태예요.
                    이 두 신호가 함께 높을 때는 새로운 성분이나 집중 케어를 추가하는 것보다
                    지금 루틴을 단순화하는 게 회복에 더 효과적이에요.
                    기본 클렌징 → 보습 → 선케어 3단계로 줄이고, 세라마이드·판테놀 성분으로 장벽을 채우는 것부터 시작해보세요.
                    피부가 안정되면 그때 기능성 케어를 하나씩 도입하는 게 맞는 순서예요.
                    """.trim();
        }

        if (highMetrics.contains(SkinMetric.BARRIER)) {
            return """
                    피부 장벽이 약해진 신호가 나타나고 있어요.
                    장벽이 약해지면 수분이 빠르게 날아가고 외부 자극에 더 쉽게 반응해요.
                    지금은 루틴에 무언가를 추가하기보다 자극 요소를 먼저 줄이는 게 우선이에요.
                    클렌징 빈도와 강도를 점검하고, 보습은 세라마이드 함유 제품으로 교체해보세요.
                    """.trim();
        }

        return """
                자극 반응이 자주 나타나는 민감한 상태예요.
                지금 피부는 많은 성분을 동시에 처리하기 어려운 상태일 수 있어요.
                사용 중인 제품 수를 줄이고, 향료·알코올 없는 제품으로 정리하는 것만으로도 피부가 안정될 수 있어요.
                효능이 좋은 성분도 지금 피부가 받아들일 수 없다면 자극이 될 수 있어요.
                """.trim();
    }

    private String balancedSummary(String pairKey) {
        return switch (pairKey) {
            case "DRYNESS+OILINESS" ->
                    """
                    수분이 부족한데 피지는 많은 복합 상태예요.
                    이 패턴은 '피부가 수분이 부족해서 피지로 보충하려는' 상태일 가능성이 있어요.
                    강한 피지 제거 케어는 오히려 피지 분비를 늘릴 수 있어요.
                    가벼운 수분 공급을 늘리면 피지 과분비가 자연스럽게 줄어드는 경우가 많아요.
                    오일프리지만 수분감 있는 제형이 지금 피부에 맞아요.
                    """.trim();
            case "DRYNESS+BLEMISH_PRONENESS" ->
                    """
                    건조하면서 트러블도 반복되는 패턴이에요.
                    건조한 피부에서 트러블이 생기는 건 역설적으로 보이지만 실제로 자주 있어요.
                    수분 부족으로 각질이 두꺼워지면 모공이 막히면서 트러블로 이어질 수 있어요.
                    트러블 케어보다 보습이 먼저예요. 보습이 안정되면 트러블도 줄어드는 경우가 많아요.
                    논코메도제닉 제품으로 수분을 공급하는 게 지금 접근 방향이에요.
                    """.trim();
            case "DRYNESS+AGING_SIGNS" ->
                    """
                    건조함과 탄력 저하 신호가 함께 나타나고 있어요.
                    탄력 저하는 콜라겐 감소 외에 만성적인 수분 부족도 원인이에요.
                    기능성 항노화 성분을 쓰기 전에 보습 기반을 탄탄하게 만드는 게 효과를 높이는 방법이에요.
                    히알루론산으로 수분을 채우고, 세라마이드로 잠근 뒤 레티놀·펩타이드를 도입하는 순서가 맞아요.
                    """.trim();
            case "DRYNESS+PIGMENTATION_TONE" ->
                    """
                    건조한 피부는 같은 색소침착도 더 칙칙하게 보여요.
                    각질이 쌓이면 빛 반사가 고르지 않아 톤이 더 어두워 보이거든요.
                    미백 성분을 추가하기 전에 보습과 각질 케어를 먼저 챙기면 즉각적인 톤업 효과도 볼 수 있어요.
                    순한 AHA 성분으로 각질을 정리하고, 나이아신아마이드로 톤을 정돈하는 방향이 좋아요.
                    """.trim();
            case "OILINESS+BLEMISH_PRONENESS" ->
                    """
                    피지가 많고 트러블도 반복되는 패턴이에요.
                    피지 자체가 트러블의 직접 원인은 아니에요. 피지 + 모공 막힘 + 세균 증식이 함께 일어날 때 트러블이 생겨요.
                    피지를 억제하는 것보다 모공이 막히지 않게 하는 케어가 더 효과적이에요.
                    살리실산(BHA)은 모공 안쪽 피지와 각질을 동시에 정리해줘서 이 패턴에 잘 맞는 성분이에요.
                    """.trim();
            case "OILINESS+PIGMENTATION_TONE" ->
                    """
                    피지가 많은 환경에서는 트러블 흔적이 색소로 남기 쉬워요.
                    피지 조절이 결국 색소 케어로도 이어지는 패턴이에요.
                    피지 케어와 미백 케어를 따로 접근하기보다, 나이아신아마이드처럼
                    두 효과를 동시에 가진 성분 중심으로 루틴을 정리해보세요.
                    """.trim();
            case "BLEMISH_PRONENESS+PIGMENTATION_TONE" ->
                    """
                    트러블 흔적이 색소침착으로 이어지는 패턴이에요.
                    이 경우 색소 자체를 공략하기 전에 트러블 반복을 줄이는 게 먼저예요.
                    트러블이 계속 생기는 한 색소 케어 효과는 제한적이에요.
                    트러블 → 흉터 → 색소 사이클을 끊는 게 목표예요.
                    손으로 짜거나 건드리지 않는 것만으로도 색소 악화를 상당히 줄일 수 있어요.
                    """.trim();
            case "BLEMISH_PRONENESS+AGING_SIGNS" ->
                    """
                    트러블 케어와 항노화 케어를 동시에 해야 하는 상태예요.
                    두 고민에 효과적인 성분들이 겹쳐요. 레티놀은 트러블과 주름 모두에 작용하고,
                    나이아신아마이드는 트러블, 색소, 탄력에 모두 도움이 돼요.
                    단, 이 성분들은 장벽이 안정된 상태에서 도입해야 효과적이에요.
                    지금 장벽 상태를 먼저 확인하고 순서를 정하세요.
                    """.trim();
            case "PIGMENTATION_TONE+AGING_SIGNS" ->
                    """
                    탄력 저하와 색소침착이 함께 있는 상태예요.
                    두 고민의 공통 원인 중 하나는 자외선이에요. 선케어 하나로 두 고민을 동시에 방어할 수 있어요.
                    레티노이드는 세포 재생을 촉진해서 탄력과 색소 둘 다에 효과적이에요.
                    비타민C는 항산화로 색소를 억제하면서 콜라겐 합성도 도와줘요.
                    이 두 성분이 지금 루틴의 핵심이 될 수 있어요.
                    """.trim();
            default -> null;
        };
    }

    private TextBlock categoryText(SkinMetric metric, MetricLevel level) {
        return switch (metric) {
            case DRYNESS -> switch (level) {
                case LOW -> new TextBlock(
                        "건조 불편 신호가 낮게 관측됐어요.",
                        "피부의 수분 유지 능력이 좋은 편이에요. 가벼운 보습으로도 충분히 유지될 가능성이 높아요. 단, 환절기나 냉난방이 강한 환경에서는 주기적으로 상태를 확인해보세요."
                );
                case MID -> new TextBlock(
                        "특정 상황에서 건조함이 반복되는 편이에요.",
                        "피부가 수분을 잃는 상황이 있어요. 세안 후나 건조한 환경에서 당김이 느껴진다면 보습을 미루지 않는 습관이 중요해요. 지금 루틴이 피부를 충분히 채우고 있는지 확인해볼 시점이에요."
                );
                case HIGH -> new TextBlock(
                        "당김과 건조 패턴이 뚜렷하게 나타나고 있어요.",
                        "단순한 수분 부족이 아니라 수분을 잡아두는 능력 자체가 약해졌을 가능성이 있어요. 보습제의 양보다 성분이 중요한 상태예요. 세라마이드, 히알루론산처럼 수분을 붙잡아주는 성분이 도움이 될 수 있어요."
                );
            };
            case OILINESS -> switch (level) {
                case LOW -> new TextBlock(
                        "피지 과다 신호가 낮아요.",
                        "피지 분비가 적은 편이에요. 오히려 피지는 피부의 자연 보호막 역할을 해요. 과도한 클렌징이나 피지 제거 제품은 피부 균형을 무너뜨릴 수 있으니 주의하세요."
                );
                case MID -> new TextBlock(
                        "T존이나 특정 상황에서 유분 증가가 나타나는 편이에요.",
                        "전형적인 복합성 피부 패턴이에요. 피지가 많은 부위와 건조한 부위를 다르게 관리하는 게 효과적이에요. 전체에 동일한 제품을 바르는 방식보다 부위별 접근을 고려해보세요."
                );
                case HIGH -> new TextBlock(
                        "전반적인 피지 과다 신호가 뚜렷해요.",
                        "피지 분비 자체를 억제하려는 강한 케어는 피부가 더 많은 피지를 분비하는 악순환을 만들 수 있어요. 수분 공급으로 피지 과분비의 원인을 줄이는 접근이 장기적으로 더 효과적이에요. 논코메도제닉(모공 막힘 없는) 제품 선택이 중요해요."
                );
            };
            case BLEMISH_PRONENESS -> switch (level) {
                case LOW -> new TextBlock(
                        "반복 트러블 경향 신호가 낮아요.",
                        "지금 피부 환경이 트러블에 취약하지 않은 상태예요. 예방 차원에서 모공을 막는 성분(코코넛 오일, 라놀린 등)이 든 제품은 피하는 게 좋아요."
                );
                case MID -> new TextBlock(
                        "주기적이거나 부위성 트러블 신호가 있어요.",
                        "트러블 발생 패턴에 규칙성이 있을 수 있어요. 생리 주기, 스트레스, 특정 음식과의 연관성을 체크해보세요. 부위별로 원인이 달라요. 턱 주변은 호르몬, 이마는 피지, 볼은 마찰이나 건조가 주요 원인이에요."
                );
                case HIGH -> new TextBlock(
                        "반복적인 트러블 경향 신호가 강하게 나타나고 있어요.",
                        "트러블을 빠르게 없애려는 강한 케어가 오히려 피부 장벽을 약화시켜 트러블을 반복시킬 수 있어요. 살리실산, 나이아신아마이드 같은 성분은 자극 없이 트러블 환경을 개선해줘요. 트러블 케어와 장벽 케어를 동시에 진행하는 루틴이 필요한 상태예요."
                );
            };
            case SENSITIVITY -> switch (level) {
                case LOW -> new TextBlock(
                        "제품·환경 자극에 크게 반응하지 않는 편이에요.",
                        "피부 자극 허용도가 높은 편이에요. 다양한 성분 시도에 비교적 유연한 상태예요. 다만 새로운 성분은 항상 소량 패치 테스트를 먼저 하는 습관을 유지하세요."
                );
                case MID -> new TextBlock(
                        "일부 자극에서 불편 반응이 나타날 수 있어요.",
                        "특정 조건이나 성분에서 반응이 생기는 경향이 있어요. 향료, 알코올, 강한 산성 성분에서 반응이 생기는 경우가 많아요. 새 제품은 한 번에 하나씩 도입해서 어떤 성분에서 반응하는지 파악해두는 게 좋아요."
                );
                case HIGH -> new TextBlock(
                        "자극 반응이 자주, 뚜렷하게 나타나고 있어요.",
                        "지금 피부는 많은 성분을 동시에 감당하기 어려운 상태일 수 있어요. 루틴을 단순화하는 게 역설적으로 피부를 더 빨리 안정시켜요. 성분 수가 적고, 향료·알코올·인공색소가 없는 제품 위주로 정리해보세요. 레티놀, AHA/BHA 같은 강한 기능성 성분은 피부가 안정된 후에 도입하세요."
                );
            };
            case BARRIER -> switch (level) {
                case LOW -> new TextBlock(
                        "장벽 부담 신호가 낮아요.",
                        "피부 보호막이 비교적 잘 유지되고 있어요. 과도한 각질 제거나 잦은 클렌징은 장벽을 약화시킬 수 있으니 필요 이상의 케어는 줄이세요."
                );
                case MID -> new TextBlock(
                        "건조·민감 신호가 일부 겹쳐 있어요.",
                        "장벽이 완전히 약해진 건 아니지만 관리가 필요한 시점이에요. 세라마이드, 판테놀처럼 장벽을 직접 채워주는 성분이 든 제품이 도움이 돼요. 지금 쓰는 제품 중 자극이 될 수 있는 성분(알코올, 강한 향료)이 있다면 점검해보세요."
                );
                case HIGH -> new TextBlock(
                        "건조와 민감 신호가 함께 높아 장벽 부담 가능성이 커요.",
                        "장벽이 약해지면 수분이 빠르게 날아가고, 외부 자극에도 쉽게 반응해요. 지금은 새로운 케어를 추가하기보다 루틴을 줄이는 게 회복에 더 빠르게 작용해요. 클렌징은 자극 없이 순하게, 보습은 세라마이드 함유 제품으로, 기능성 성분(레티놀, 산 계열)은 장벽 회복 후로 미루세요."
                );
            };
            case PIGMENTATION_TONE -> switch (level) {
                case LOW -> new TextBlock(
                        "톤·색소 고민 신호가 낮아요.",
                        "현재 색소침착이나 톤 불균일 고민이 크지 않은 상태예요. 지금 상태를 유지하는 가장 효과적인 방법은 자외선 차단이에요. SPF 30 이상 선크림을 매일 바르는 것만으로 색소 악화를 80% 이상 예방할 수 있어요."
                );
                case MID -> new TextBlock(
                        "톤 불균일이나 잡티 고민 신호가 있어요.",
                        "색소침착은 자외선 노출 누적이 주원인이에요. 선케어를 가장 먼저 챙기고, 나이아신아마이드(멜라닌 전달 억제)나 비타민C(산화 억제) 성분이 도움이 돼요. 트러블 흔적이 색소로 이어지는 패턴이라면 트러블 케어가 미백 케어보다 우선이에요."
                );
                case HIGH -> new TextBlock(
                        "톤·색소 고민 신호가 뚜렷하게 나타나고 있어요.",
                        "이미 생긴 색소침착은 개선에 시간이 걸려요. 빠른 효과를 기대하며 강한 성분을 시도하면 오히려 색소가 더 짙어질 수 있어요(PIH 악화). 선케어를 철저히 하면서 나이아신아마이드, 알부틴처럼 자극 없이 작용하는 미백 성분부터 꾸준히 써보세요. 레티노이드 계열은 효과적이지만 피부 적응 기간이 필요해요."
                );
            };
            case AGING_SIGNS -> switch (level) {
                case LOW -> new TextBlock(
                        "탄력·주름 고민 신호가 낮아요.",
                        "지금 노화 징후가 크게 느껴지지 않더라도, 예방적 루틴의 효과는 지금 시작할수록 커요. 자외선 차단과 보습이 가장 강력한 항노화 루틴이에요. 레티놀은 지금부터 저농도로 시작하면 나중에 더 효과적으로 활용할 수 있어요."
                );
                case MID -> new TextBlock(
                        "초기 노화 고민 신호가 있어요.",
                        "탄력 저하와 잔주름은 피부 세포 회전 주기가 느려지면서 시작돼요. 레티놀, 펩타이드 성분이 세포 재생을 도와줘요. 보습이 선행되지 않으면 기능성 성분의 효과도 반감되니 루틴 순서가 중요해요."
                );
                case HIGH -> new TextBlock(
                        "탄력·주름 고민 신호가 뚜렷하게 나타나고 있어요.",
                        "항노화 케어에서 가장 효과가 입증된 성분은 레티노이드예요. 다만 자극이 있어서 장벽이 약한 상태라면 먼저 장벽을 안정시키고 도입해야 해요. 레티놀 → 레티날 → 레티노산 순으로 효과와 자극이 강해지니 단계적 접근이 맞아요. 선케어는 항노화 루틴의 절반이에요. 아무리 좋은 성분도 자외선 차단 없이는 효과가 줄어요."
                );
            };
        };
    }

    private SkinMetric firstSecondary(List<SkinMetric> highMetrics, SkinMetric primary) {
        return highMetrics.stream()
                .filter(metric -> metric != primary)
                .findFirst()
                .orElse(null);
    }

    private String pairKey(SkinMetric first, SkinMetric second) {
        return first.name() + "+" + second.name();
    }

    private enum MetricLevel {
        LOW,
        MID,
        HIGH
    }

    private record ScoredMetric(
            SkinMetric metric,
            int score,
            MetricLevel level
    ) {
    }

    private record TextBlock(
            String stateText,
            String insight
    ) {
    }
}
