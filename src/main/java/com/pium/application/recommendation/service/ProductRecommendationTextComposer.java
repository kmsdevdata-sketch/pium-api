package com.pium.application.recommendation.service;

import com.pium.application.recommendation.dto.ProductRecommendationDetailView;
import com.pium.application.recommendation.dto.ProductRecommendationListView;
import com.pium.domain.productprofile.enumtype.ProductRiskTrait;
import com.pium.domain.productprofile.enumtype.RecommendationTrait;
import com.pium.domain.productprofile.model.EvidenceSignal;
import com.pium.domain.productprofile.model.ProductProfile;
import com.pium.domain.productprofile.model.ProductTraitSignal;
import com.pium.domain.recommendation.enumtype.ScoreBand;
import com.pium.domain.recommendation.model.interpretation.RiskConstraint;
import com.pium.domain.recommendation.model.interpretation.SkinInterpretation;
import com.pium.domain.recommendation.model.interpretation.SkinNeed;
import com.pium.domain.recommendation.model.scoring.AppliedRisk;
import com.pium.domain.recommendation.model.scoring.MatchedTrait;
import com.pium.domain.recommendation.model.scoring.ScoredRecommendationCandidate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ProductRecommendationTextComposer {

    public static final String AD_DISCLOSURE =
            "이 포스팅은 올리브영 쇼핑 큐레이터 활동의 일환으로, 구매 시 일정 금액의 수수료를 제공받습니다.";

    ProductRecommendationListView.RecommendationSummaryView summary(
            SkinInterpretation interpretation,
            boolean recommendationEmpty
    ) {
        return new ProductRecommendationListView.RecommendationSummaryView(
                headline(interpretation.routineIntent()),
                summaryReasons(interpretation),
                summaryNotices(recommendationEmpty)
        );
    }

    String scoreBandLabel(ScoreBand scoreBand) {
        return switch (scoreBand) {
            case HIGH -> "잘 맞음";
            case MEDIUM -> "무난함";
            case LOW -> "가볍게 참고";
        };
    }

    String careLabel(RecommendationTrait trait) {
        return switch (trait) {
            case HYDRATION_SUPPORT -> "수분 충전";
            case BARRIER_SUPPORT -> "장벽 케어";
            case SOOTHING_SUPPORT -> "진정 케어";
            case SEBUM_CONTROL_SUPPORT -> "피지 밸런스";
            case BLEMISH_CARE_SUPPORT -> "트러블 케어";
            case BRIGHTENING_SUPPORT -> "톤 케어";
            case ANTI_AGING_SUPPORT -> "탄력 케어";
            case UV_PROTECTION -> "자외선 차단";
            case EXFOLIATION_EFFECT -> "결 정돈";
        };
    }

    String cautionLabel(ProductRiskTrait trait) {
        return switch (trait) {
            case IRRITATION_RISK -> "자극 주의";
            case HIGH_IRRITATION_RISK -> "높은 자극 주의";
            case FRAGRANCE_OR_ALLERGEN_RISK -> "향 성분 주의";
            case COMEDOGENIC_RISK -> "모공 부담 주의";
            case DRYING_OR_STRIPPING_RISK -> "건조감 주의";
            case STRONG_EXFOLIATION_EFFECT -> "강한 각질 케어 주의";
            case STRONG_ACTIVE_RISK -> "고기능 성분 주의";
            case HEAVY_OCCLUSIVE_RISK -> "무거운 사용감 주의";
        };
    }

    String itemReason(ScoredRecommendationCandidate candidate) {
        Optional<RecommendationTrait> trait = primaryMatchedTrait(candidate);
        if (trait.isPresent()) {
            return reasonFor(trait.get());
        }
        if (!candidate.penaltyRisks().isEmpty() || !candidate.cautionRisks().isEmpty()) {
            return "피부 조건과 일부 맞는 후보지만, 사용 전 확인할 포인트가 있어 함께 표시했어요.";
        }
        return "최근 진단에서 강한 부담 신호가 적어 기본 관리 후보로 반영했어요.";
    }

    List<ProductRecommendationDetailView.ReasonDetailView> reasonDetails(
            ScoredRecommendationCandidate candidate,
            ProductProfile profile
    ) {
        Optional<RecommendationTrait> trait = primaryMatchedTrait(candidate);
        if (trait.isEmpty()) {
            return List.of(new ProductRecommendationDetailView.ReasonDetailView(
                    "추천에 반영한 방식",
                    "피부 조건과 상품 포인트가 일부 맞아 참고 후보로 반영했어요."
            ));
        }

        RecommendationTrait matchedTrait = trait.get();
        return List.of(
                new ProductRecommendationDetailView.ReasonDetailView(
                        "진단에서 본 점",
                        diagnosisReasonFor(matchedTrait)
                ),
                new ProductRecommendationDetailView.ReasonDetailView(
                        "상품에서 확인한 점",
                        productEvidenceFor(profile, matchedTrait)
                ),
                new ProductRecommendationDetailView.ReasonDetailView(
                        "추천에 반영한 방식",
                        "그래서 " + careLabel(matchedTrait) + " 포인트는 추천에 반영하고, 사용 전 주의할 신호는 함께 확인했어요."
                )
        );
    }

    List<String> recommendationReasons(ScoredRecommendationCandidate candidate) {
        return primaryMatchedTrait(candidate)
                .map(trait -> List.of(reasonFor(trait)))
                .orElseGet(() -> List.of("피부 조건과 상품 포인트가 일부 맞아 참고 후보로 반영했어요."));
    }

    List<String> cautions(ScoredRecommendationCandidate candidate) {
        List<String> cautions = Stream.concat(
                        candidate.penaltyRisks().stream(),
                        candidate.cautionRisks().stream()
                )
                .sorted(Comparator.comparing(appliedRisk -> appliedRisk.policy().ordinal()))
                .map(this::cautionFor)
                .distinct()
                .toList();

        if (cautions.isEmpty()) {
            return List.of("현재 매칭 기준에서 크게 낮춰야 할 주의 신호는 적어요.");
        }
        return cautions;
    }

    List<String> careTags(ScoredRecommendationCandidate candidate) {
        return matchedTraits(candidate).stream()
                .map(MatchedTrait::trait)
                .distinct()
                .map(this::careLabel)
                .limit(3)
                .toList();
    }

    List<String> cautionPoints(ScoredRecommendationCandidate candidate) {
        return appliedRisks(candidate).stream()
                .map(AppliedRisk::trait)
                .distinct()
                .map(this::cautionLabel)
                .limit(2)
                .toList();
    }

    List<ProductRecommendationDetailView.TagView> careTagViews(ScoredRecommendationCandidate candidate) {
        return matchedTraits(candidate).stream()
                .map(MatchedTrait::trait)
                .distinct()
                .map(trait -> new ProductRecommendationDetailView.TagView(trait.name(), careLabel(trait)))
                .toList();
    }

    List<ProductRecommendationDetailView.TagView> cautionPointViews(ScoredRecommendationCandidate candidate) {
        return appliedRisks(candidate).stream()
                .map(AppliedRisk::trait)
                .distinct()
                .map(trait -> new ProductRecommendationDetailView.TagView(trait.name(), cautionLabel(trait)))
                .toList();
    }

    private String headline(SkinInterpretation.RoutineIntent intent) {
        return switch (intent) {
            case BARRIER_RECOVERY -> "장벽 부담을 먼저 보고, 케어 포인트와 자극 부담을 함께 확인했어요.";
            case SOOTHING_FIRST -> "민감 신호를 기준으로, 진정 포인트와 자극 부담을 함께 확인했어요.";
            case HYDRATION_BALANCE -> "건조 신호를 기준으로, 수분 케어 포인트와 사용 전 주의점을 함께 봤어요.";
            case SEBUM_BLEMISH_BALANCE -> "피지와 트러블 신호를 함께 보고, 부담이 적은 케어 포인트를 골랐어요.";
            case TONE_CARE -> "톤 케어 목표와 피부 부담 신호를 함께 보고 상품을 골랐어요.";
            case ANTI_AGING_CARE -> "탄력 케어 목표와 피부 부담 신호를 함께 보고 상품을 골랐어요.";
            case BASIC_BALANCE -> "강한 부담 신호가 적어, 기본 케어에 맞는 상품을 중심으로 골랐어요.";
        };
    }

    private List<String> summaryReasons(SkinInterpretation interpretation) {
        List<String> reasons = Stream.concat(
                        interpretation.primaryNeeds().stream(),
                        interpretation.secondaryNeeds().stream()
                )
                .map(SkinNeed::trait)
                .distinct()
                .limit(2)
                .map(this::diagnosisReasonFor)
                .toList();

        if (!reasons.isEmpty()) {
            return reasons;
        }
        return List.of("최근 진단 결과에서 강하게 치우친 신호가 적어 기본 관리 방향으로 정리했어요.");
    }

    private List<String> summaryNotices(boolean recommendationEmpty) {
        List<String> notices = new ArrayList<>();
        if (recommendationEmpty) {
            notices.add("현재 등록된 상품 중 이번 진단 조건에 충분히 맞는 후보가 부족해요.");
        }
        notices.add("추천은 화장품 선택 참고용이며 의학적 진단이 아니에요.");
        notices.add("피부 반응은 개인마다 다를 수 있어 새 제품은 적은 양으로 먼저 사용해보세요.");
        return notices;
    }

    private Optional<RecommendationTrait> primaryMatchedTrait(ScoredRecommendationCandidate candidate) {
        return matchedTraits(candidate).stream()
                .map(MatchedTrait::trait)
                .findFirst();
    }

    private List<MatchedTrait> matchedTraits(ScoredRecommendationCandidate candidate) {
        return Stream.of(
                        candidate.matchedRequiredTraits(),
                        candidate.matchedGoalTraits(),
                        candidate.matchedPreferredTraits()
                )
                .flatMap(List::stream)
                .distinct()
                .toList();
    }

    private List<AppliedRisk> appliedRisks(ScoredRecommendationCandidate candidate) {
        return Stream.concat(candidate.penaltyRisks().stream(), candidate.cautionRisks().stream())
                .distinct()
                .toList();
    }

    private String reasonFor(RecommendationTrait trait) {
        return switch (trait) {
            case HYDRATION_SUPPORT -> "건조 신호가 높아, 상품의 수분 케어 포인트가 확인된 후보를 우선 매칭했어요.";
            case BARRIER_SUPPORT -> "장벽 부담 신호가 있어, 장벽 케어 포인트가 있는 상품을 우선 매칭했어요.";
            case SOOTHING_SUPPORT -> "민감하게 반응할 수 있는 신호가 있어, 진정 케어 포인트를 우선 확인했어요.";
            case SEBUM_CONTROL_SUPPORT -> "피지 신호를 고려해, 유분 균형을 돕는 포인트가 있는 상품을 반영했어요.";
            case BLEMISH_CARE_SUPPORT -> "트러블 고민과 피부 부담을 함께 보고, 트러블 케어 포인트를 반영했어요.";
            case BRIGHTENING_SUPPORT -> "톤 케어 목표와 피부 신호를 함께 보고, 톤 케어 포인트를 반영했어요.";
            case ANTI_AGING_SUPPORT -> "탄력·주름 고민과 피부 부담을 함께 보고, 탄력 케어 포인트를 반영했어요.";
            case UV_PROTECTION -> "톤과 탄력 고민에 영향을 줄 수 있어, 자외선 차단 포인트를 함께 반영했어요.";
            case EXFOLIATION_EFFECT -> "결 정돈 목표는 반영하되, 피부 부담 신호와 함께 확인했어요.";
        };
    }

    private String diagnosisReasonFor(RecommendationTrait trait) {
        return switch (trait) {
            case HYDRATION_SUPPORT -> "최근 진단에서 건조 신호가 높게 보여 수분을 먼저 채우는 방향이 필요해요.";
            case BARRIER_SUPPORT -> "최근 진단에서 장벽 부담 신호가 보여 피부를 편안하게 유지하는 방향을 함께 봤어요.";
            case SOOTHING_SUPPORT -> "최근 진단에서 민감하게 반응할 수 있는 신호가 보여 자극 부담을 낮추는 방향을 봤어요.";
            case SEBUM_CONTROL_SUPPORT -> "최근 진단에서 피지 균형과 관련된 신호가 보여 유분 밸런스를 함께 봤어요.";
            case BLEMISH_CARE_SUPPORT -> "최근 진단과 선택한 고민에서 트러블 케어 방향을 확인했어요.";
            case BRIGHTENING_SUPPORT -> "선택한 고민과 피부 톤 신호를 함께 보고 톤 케어 방향을 확인했어요.";
            case ANTI_AGING_SUPPORT -> "탄력·주름 고민과 관련된 신호를 보고 탄력 케어 방향을 확인했어요.";
            case UV_PROTECTION -> "톤과 탄력 고민을 고려해 자외선 차단 포인트를 함께 봤어요.";
            case EXFOLIATION_EFFECT -> "결 정돈 목표는 반영하되 피부 부담 가능성도 함께 봤어요.";
        };
    }

    private String productEvidenceFor(ProductProfile profile, RecommendationTrait trait) {
        Optional<ProductTraitSignal<RecommendationTrait>> signal = profile.benefitTraits().stream()
                .filter(benefit -> benefit.trait() == trait)
                .findFirst();

        if (signal.isEmpty()) {
            return "상품 프로파일에서 " + careLabel(trait) + " 포인트가 확인됐어요.";
        }

        return signal.get().evidenceRefs().stream()
                .flatMap(ref -> profile.evidenceSignals().stream()
                        .filter(evidence -> evidence.id().equals(ref))
                        .map(EvidenceSignal::message))
                .findFirst()
                .orElse("상품 프로파일에서 " + careLabel(trait) + " 포인트가 확인됐어요.");
    }

    private String cautionFor(AppliedRisk risk) {
        String label = cautionLabel(risk.trait());
        String suffix = risk.policy() == RiskConstraint.Policy.SOFT_PENALTY
                ? "가 현재 피부 상태에 부담이 될 수 있어 순위를 낮추고 참고점으로 표시했어요."
                : "가 확인돼 현재 피부 상태에서는 사용 전 참고할 점으로 표시했어요.";
        return label + suffix;
    }
}
