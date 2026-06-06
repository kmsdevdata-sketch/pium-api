package com.pium.adapter.inbound.web.admin.user;

import java.time.LocalDateTime;
import java.util.List;

public final class AdminUserInsightResponse {

    private AdminUserInsightResponse() {
    }

    public record Summary(
            long totalUserCount,
            long activeUserCount,
            long withdrawnUserCount,
            long bannedUserCount,
            long recent7DaysNewUserCount,
            long recent30DaysNewUserCount,
            long totalDiagnosisCount,
            long diagnosedUserCount,
            long undiagnosedUserCount,
            long recent7DaysDiagnosisCount,
            long recent30DaysDiagnosisCount,
            List<AnalysisTypeCount> analysisTypeCounts,
            List<GoalCount> topGoals
    ) {
    }

    public record UserList(
            long totalCount,
            int page,
            int size,
            List<UserItem> users
    ) {
    }

    public record UserItem(
            String userId,
            String nickname,
            String profileImageUrl,
            String provider,
            String status,
            String role,
            LocalDateTime createdAt,
            LocalDateTime lastLoginAt,
            long diagnosisCount,
            LocalDateTime latestDiagnosisAt,
            String latestAnalysisType,
            List<String> goals
    ) {
    }

    public record UserDetail(
            String userId,
            String nickname,
            String profileImageUrl,
            String provider,
            String providerUserIdMasked,
            String status,
            String role,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime lastLoginAt,
            long diagnosisCount,
            DiagnosisSummary latestDiagnosisSummary,
            List<GoalCount> goalSummary
    ) {
    }

    public record DiagnosisSummary(
            String resultId,
            String analysisType,
            LocalDateTime createdAt,
            List<String> goals,
            List<SkinMetricScore> skinMetricScores
    ) {
    }

    public record SkinAnalysisResultList(
            long totalCount,
            int page,
            int size,
            List<DiagnosisSummary> results
    ) {
    }

    public record SkinMetricScore(
            String metric,
            int score
    ) {
    }

    public record GoalInsight(
            List<GoalCount> goals
    ) {
    }

    public record GoalCount(
            String goalCode,
            String goalLabel,
            long count,
            double rate
    ) {
    }

    public record SkinMetricInsight(
            List<SkinMetricDistribution> metrics
    ) {
    }

    public record SkinMetricDistribution(
            String metric,
            double averageScore,
            long lowCount,
            long midCount,
            long highCount,
            double lowRate,
            double midRate,
            double highRate
    ) {
    }

    public record AnalysisTypeCount(
            String analysisType,
            long count
    ) {
    }
}
