package com.pium.adapter.inbound.web.admin.user;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AdminUserInsightQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AdminUserInsightQueryService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AdminUserInsightResponse.Summary summary() {
        long totalUserCount = count("SELECT COUNT(*) FROM users", params());
        long activeUserCount = count("SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'", params());
        long withdrawnUserCount = count("SELECT COUNT(*) FROM users WHERE status = 'WITHDRAWN'", params());
        long bannedUserCount = count("SELECT COUNT(*) FROM users WHERE status = 'BANNED'", params());
        long recent7DaysNewUserCount = count(
                "SELECT COUNT(*) FROM users WHERE created_at >= :from",
                params().addValue("from", LocalDateTime.now().minusDays(7))
        );
        long recent30DaysNewUserCount = count(
                "SELECT COUNT(*) FROM users WHERE created_at >= :from",
                params().addValue("from", LocalDateTime.now().minusDays(30))
        );
        long totalDiagnosisCount = count("SELECT COUNT(*) FROM skin_analysis_result", params());
        long diagnosedUserCount = count(
                "SELECT COUNT(DISTINCT user_id) FROM skin_analysis_result",
                params()
        );
        long recent7DaysDiagnosisCount = count(
                "SELECT COUNT(*) FROM skin_analysis_result WHERE created_at >= :from",
                params().addValue("from", LocalDateTime.now().minusDays(7))
        );
        long recent30DaysDiagnosisCount = count(
                "SELECT COUNT(*) FROM skin_analysis_result WHERE created_at >= :from",
                params().addValue("from", LocalDateTime.now().minusDays(30))
        );

        return new AdminUserInsightResponse.Summary(
                totalUserCount,
                activeUserCount,
                withdrawnUserCount,
                bannedUserCount,
                recent7DaysNewUserCount,
                recent30DaysNewUserCount,
                totalDiagnosisCount,
                diagnosedUserCount,
                totalUserCount - diagnosedUserCount,
                recent7DaysDiagnosisCount,
                recent30DaysDiagnosisCount,
                analysisTypeCounts(null, null, null),
                goalCounts(null, null, null, 5)
        );
    }

    public AdminUserInsightResponse.UserList list(UserSearchCondition condition) {
        UserSearchCondition normalized = condition.normalize();
        MapSqlParameterSource parameters = params()
                .addValue("limit", normalized.size())
                .addValue("offset", normalized.page() * normalized.size());
        String where = userWhere(normalized, parameters);
        String from = """
                FROM users u
                LEFT JOIN user_profile up ON up.user_id = u.user_id
                LEFT JOIN user_oauth uo ON uo.user_id = u.user_id
                LEFT JOIN (
                    SELECT user_id, COUNT(*) AS diagnosis_count, MAX(created_at) AS latest_diagnosis_at
                    FROM skin_analysis_result
                    GROUP BY user_id
                ) ds ON ds.user_id = u.user_id
                LEFT JOIN skin_analysis_result latest
                    ON latest.user_id = u.user_id
                   AND latest.created_at = ds.latest_diagnosis_at
                """;

        long totalCount = count("SELECT COUNT(DISTINCT u.user_id) " + from + where, parameters);
        List<AdminUserInsightResponse.UserItem> users = jdbcTemplate.query("""
                        SELECT DISTINCT
                               u.user_id,
                               up.nickname,
                               up.profile_image_url,
                               uo.provider,
                               u.status,
                               u.role,
                               u.created_at,
                               uo.last_login_at,
                               COALESCE(ds.diagnosis_count, 0) AS diagnosis_count,
                               ds.latest_diagnosis_at,
                               latest.analysis_type AS latest_analysis_type
                        """ + from + where + orderBy(normalized.sort()) + """
                        LIMIT :limit OFFSET :offset
                        """,
                parameters,
                (rs, rowNum) -> new AdminUserInsightResponse.UserItem(
                        rs.getString("user_id"),
                        rs.getString("nickname"),
                        rs.getString("profile_image_url"),
                        rs.getString("provider"),
                        rs.getString("status"),
                        rs.getString("role"),
                        localDateTime(rs, "created_at"),
                        localDateTime(rs, "last_login_at"),
                        rs.getLong("diagnosis_count"),
                        localDateTime(rs, "latest_diagnosis_at"),
                        rs.getString("latest_analysis_type"),
                        goalsByUser(rs.getString("user_id"))
                ));

        return new AdminUserInsightResponse.UserList(
                totalCount,
                normalized.page(),
                normalized.size(),
                users
        );
    }

    public AdminUserInsightResponse.UserDetail get(String userId) {
        MapSqlParameterSource parameters = params().addValue("userId", userId);
        AdminUserInsightResponse.UserDetail base = jdbcTemplate.queryForObject("""
                        SELECT u.user_id,
                               up.nickname,
                               up.profile_image_url,
                               uo.provider,
                               uo.provider_user_id,
                               u.status,
                               u.role,
                               u.created_at,
                               u.updated_at,
                               uo.last_login_at,
                               (
                                   SELECT COUNT(*)
                                   FROM skin_analysis_result sar
                                   WHERE sar.user_id = u.user_id
                               ) AS diagnosis_count
                        FROM users u
                        LEFT JOIN user_profile up ON up.user_id = u.user_id
                        LEFT JOIN user_oauth uo ON uo.user_id = u.user_id
                        WHERE u.user_id = :userId
                        """,
                parameters,
                (rs, rowNum) -> new AdminUserInsightResponse.UserDetail(
                        rs.getString("user_id"),
                        rs.getString("nickname"),
                        rs.getString("profile_image_url"),
                        rs.getString("provider"),
                        maskProviderUserId(rs.getString("provider_user_id")),
                        rs.getString("status"),
                        rs.getString("role"),
                        localDateTime(rs, "created_at"),
                        localDateTime(rs, "updated_at"),
                        localDateTime(rs, "last_login_at"),
                        rs.getLong("diagnosis_count"),
                        latestDiagnosis(userId),
                        goalSummary(userId)
                ));
        return base;
    }

    public AdminUserInsightResponse.SkinAnalysisResultList skinAnalysisResults(
            String userId,
            String analysisType,
            int page,
            int size
    ) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = normalizeSize(size);
        MapSqlParameterSource parameters = params()
                .addValue("userId", userId)
                .addValue("analysisType", blankToNull(analysisType))
                .addValue("limit", normalizedSize)
                .addValue("offset", normalizedPage * normalizedSize);
        String where = "WHERE user_id = :userId ";
        if (blankToNull(analysisType) != null) {
            where += "AND analysis_type = :analysisType ";
        }

        long totalCount = count("SELECT COUNT(*) FROM skin_analysis_result " + where, parameters);
        List<AdminUserInsightResponse.DiagnosisSummary> results = jdbcTemplate.query("""
                        SELECT result_id, analysis_type, created_at
                        FROM skin_analysis_result
                        """ + where + """
                        ORDER BY created_at DESC
                        LIMIT :limit OFFSET :offset
                        """,
                parameters,
                diagnosisSummaryMapper());

        return new AdminUserInsightResponse.SkinAnalysisResultList(
                totalCount,
                normalizedPage,
                normalizedSize,
                results
        );
    }

    public AdminUserInsightResponse.GoalInsight goals(
            LocalDateTime from,
            LocalDateTime to,
            String analysisType
    ) {
        return new AdminUserInsightResponse.GoalInsight(goalCounts(from, to, analysisType, null));
    }

    public AdminUserInsightResponse.SkinMetricInsight skinMetrics(
            LocalDateTime from,
            LocalDateTime to,
            String analysisType
    ) {
        MapSqlParameterSource parameters = insightParams(from, to, analysisType);
        String where = insightWhere(from, to, analysisType);
        long totalScoreCount = count("""
                SELECT COUNT(*)
                FROM skin_metric_score sms
                JOIN skin_analysis_result sar ON sar.result_id = sms.result_id
                """ + where,
                parameters);

        List<AdminUserInsightResponse.SkinMetricDistribution> metrics = jdbcTemplate.query("""
                        SELECT sms.metric,
                               AVG(sms.score_value) AS average_score,
                               SUM(CASE WHEN sms.score_value < 40 THEN 1 ELSE 0 END) AS low_count,
                               SUM(CASE WHEN sms.score_value >= 40 AND sms.score_value < 70 THEN 1 ELSE 0 END) AS mid_count,
                               SUM(CASE WHEN sms.score_value >= 70 THEN 1 ELSE 0 END) AS high_count
                        FROM skin_metric_score sms
                        JOIN skin_analysis_result sar ON sar.result_id = sms.result_id
                        """ + where + """
                        GROUP BY sms.metric
                        ORDER BY sms.metric
                        """,
                parameters,
                (rs, rowNum) -> {
                    long lowCount = rs.getLong("low_count");
                    long midCount = rs.getLong("mid_count");
                    long highCount = rs.getLong("high_count");
                    long metricTotal = lowCount + midCount + highCount;
                    return new AdminUserInsightResponse.SkinMetricDistribution(
                            rs.getString("metric"),
                            round(rs.getDouble("average_score")),
                            lowCount,
                            midCount,
                            highCount,
                            rate(lowCount, metricTotal),
                            rate(midCount, metricTotal),
                            rate(highCount, metricTotal)
                    );
                });

        return new AdminUserInsightResponse.SkinMetricInsight(
                totalScoreCount == 0 ? List.of() : metrics
        );
    }

    private List<AdminUserInsightResponse.AnalysisTypeCount> analysisTypeCounts(
            LocalDateTime from,
            LocalDateTime to,
            String analysisType
    ) {
        MapSqlParameterSource parameters = insightParams(from, to, analysisType);
        return jdbcTemplate.query("""
                        SELECT analysis_type, COUNT(*) AS count_value
                        FROM skin_analysis_result sar
                        """ + insightWhere(from, to, analysisType) + """
                        GROUP BY analysis_type
                        ORDER BY count_value DESC
                        """,
                parameters,
                (rs, rowNum) -> new AdminUserInsightResponse.AnalysisTypeCount(
                        rs.getString("analysis_type"),
                        rs.getLong("count_value")
                ));
    }

    private List<AdminUserInsightResponse.GoalCount> goalCounts(
            LocalDateTime from,
            LocalDateTime to,
            String analysisType,
            Integer limit
    ) {
        MapSqlParameterSource parameters = insightParams(from, to, analysisType);
        if (limit != null) {
            parameters.addValue("limit", limit);
        }
        long totalGoalCount = count("""
                SELECT COUNT(*)
                FROM skin_analysis_goal sag
                JOIN skin_analysis_result sar ON sar.result_id = sag.result_id
                """ + insightWhere(from, to, analysisType),
                parameters);

        String limitClause = limit == null ? "" : "LIMIT :limit";
        return jdbcTemplate.query("""
                        SELECT sag.goal_code, COUNT(*) AS count_value
                        FROM skin_analysis_goal sag
                        JOIN skin_analysis_result sar ON sar.result_id = sag.result_id
                        """ + insightWhere(from, to, analysisType) + """
                        GROUP BY sag.goal_code
                        ORDER BY count_value DESC, sag.goal_code ASC
                        """ + limitClause,
                parameters,
                (rs, rowNum) -> new AdminUserInsightResponse.GoalCount(
                        rs.getString("goal_code"),
                        goalLabel(rs.getString("goal_code")),
                        rs.getLong("count_value"),
                        rate(rs.getLong("count_value"), totalGoalCount)
                ));
    }

    private AdminUserInsightResponse.DiagnosisSummary latestDiagnosis(String userId) {
        return jdbcTemplate.query("""
                        SELECT result_id, analysis_type, created_at
                        FROM skin_analysis_result
                        WHERE user_id = :userId
                        ORDER BY created_at DESC
                        LIMIT 1
                        """,
                params().addValue("userId", userId),
                diagnosisSummaryMapper()
        ).stream().findFirst().orElse(null);
    }

    private List<AdminUserInsightResponse.GoalCount> goalSummary(String userId) {
        long totalGoalCount = count("""
                SELECT COUNT(*)
                FROM skin_analysis_goal sag
                JOIN skin_analysis_result sar ON sar.result_id = sag.result_id
                WHERE sar.user_id = :userId
                """,
                params().addValue("userId", userId));

        return jdbcTemplate.query("""
                        SELECT sag.goal_code, COUNT(*) AS count_value
                        FROM skin_analysis_goal sag
                        JOIN skin_analysis_result sar ON sar.result_id = sag.result_id
                        WHERE sar.user_id = :userId
                        GROUP BY sag.goal_code
                        ORDER BY count_value DESC, sag.goal_code ASC
                        """,
                params().addValue("userId", userId),
                (rs, rowNum) -> new AdminUserInsightResponse.GoalCount(
                        rs.getString("goal_code"),
                        goalLabel(rs.getString("goal_code")),
                        rs.getLong("count_value"),
                        rate(rs.getLong("count_value"), totalGoalCount)
                ));
    }

    private RowMapper<AdminUserInsightResponse.DiagnosisSummary> diagnosisSummaryMapper() {
        return (rs, rowNum) -> {
            String resultId = rs.getString("result_id");
            return new AdminUserInsightResponse.DiagnosisSummary(
                    resultId,
                    rs.getString("analysis_type"),
                    localDateTime(rs, "created_at"),
                    goalsByResult(resultId),
                    scoresByResult(resultId)
            );
        };
    }

    private List<String> goalsByUser(String userId) {
        return jdbcTemplate.queryForList("""
                        SELECT DISTINCT sag.goal_code
                        FROM skin_analysis_goal sag
                        JOIN skin_analysis_result sar ON sar.result_id = sag.result_id
                        WHERE sar.user_id = :userId
                        ORDER BY sag.goal_code
                        """,
                params().addValue("userId", userId),
                String.class);
    }

    private List<String> goalsByResult(String resultId) {
        return jdbcTemplate.queryForList("""
                        SELECT goal_code
                        FROM skin_analysis_goal
                        WHERE result_id = :resultId
                        ORDER BY goal_code
                        """,
                params().addValue("resultId", resultId),
                String.class);
    }

    private List<AdminUserInsightResponse.SkinMetricScore> scoresByResult(String resultId) {
        return jdbcTemplate.query("""
                        SELECT metric, score_value
                        FROM skin_metric_score
                        WHERE result_id = :resultId
                        ORDER BY id ASC
                        """,
                params().addValue("resultId", resultId),
                (rs, rowNum) -> new AdminUserInsightResponse.SkinMetricScore(
                        rs.getString("metric"),
                        rs.getInt("score_value")
                ));
    }

    private String userWhere(UserSearchCondition condition, MapSqlParameterSource parameters) {
        List<String> conditions = new ArrayList<>();
        if (blankToNull(condition.status()) != null) {
            conditions.add("u.status = :status");
            parameters.addValue("status", condition.status().trim().toUpperCase());
        }
        if (blankToNull(condition.provider()) != null) {
            conditions.add("uo.provider = :provider");
            parameters.addValue("provider", condition.provider().trim().toUpperCase());
        }
        if (condition.diagnosed() != null) {
            conditions.add(condition.diagnosed() ? "COALESCE(ds.diagnosis_count, 0) > 0" : "COALESCE(ds.diagnosis_count, 0) = 0");
        }
        if (blankToNull(condition.analysisType()) != null) {
            conditions.add("latest.analysis_type = :analysisType");
            parameters.addValue("analysisType", condition.analysisType().trim().toUpperCase());
        }
        if (blankToNull(condition.goal()) != null) {
            conditions.add("""
                    EXISTS (
                        SELECT 1
                        FROM skin_analysis_result sar_goal
                        JOIN skin_analysis_goal sag_goal ON sag_goal.result_id = sar_goal.result_id
                        WHERE sar_goal.user_id = u.user_id
                          AND sag_goal.goal_code = :goal
                    )
                    """);
            parameters.addValue("goal", condition.goal().trim());
        }
        if (blankToNull(condition.keyword()) != null) {
            conditions.add("(u.user_id LIKE :keyword OR up.nickname LIKE :keyword)");
            parameters.addValue("keyword", "%" + condition.keyword().trim() + "%");
        }
        return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions) + " ";
    }

    private String orderBy(String sort) {
        return switch (sort == null ? "" : sort.trim().toUpperCase()) {
            case "CREATED_AT" -> " ORDER BY u.created_at DESC ";
            case "LAST_LOGIN" -> " ORDER BY CASE WHEN uo.last_login_at IS NULL THEN 1 ELSE 0 END, uo.last_login_at DESC, u.created_at DESC ";
            case "DIAGNOSIS_COUNT" -> " ORDER BY diagnosis_count DESC, u.created_at DESC ";
            default -> " ORDER BY CASE WHEN latest_diagnosis_at IS NULL THEN 1 ELSE 0 END, latest_diagnosis_at DESC, u.created_at DESC ";
        };
    }

    private String insightWhere(LocalDateTime from, LocalDateTime to, String analysisType) {
        List<String> conditions = new ArrayList<>();
        if (from != null) {
            conditions.add("sar.created_at >= :from");
        }
        if (to != null) {
            conditions.add("sar.created_at <= :to");
        }
        if (blankToNull(analysisType) != null) {
            conditions.add("sar.analysis_type = :analysisType");
        }
        return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions) + " ";
    }

    private MapSqlParameterSource insightParams(LocalDateTime from, LocalDateTime to, String analysisType) {
        return params()
                .addValue("from", from)
                .addValue("to", to)
                .addValue("analysisType", blankToNull(analysisType) == null ? null : analysisType.trim().toUpperCase());
    }

    private long count(String sql, MapSqlParameterSource parameters) {
        Long count = jdbcTemplate.queryForObject(sql, parameters, Long.class);
        return count == null ? 0 : count;
    }

    private MapSqlParameterSource params() {
        return new MapSqlParameterSource();
    }

    private LocalDateTime localDateTime(ResultSet rs, String columnName) throws SQLException {
        var timestamp = rs.getTimestamp(columnName);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String maskProviderUserId(String providerUserId) {
        if (providerUserId == null || providerUserId.length() <= 4) {
            return providerUserId;
        }
        return providerUserId.substring(0, 2) + "***" + providerUserId.substring(providerUserId.length() - 2);
    }

    private String goalLabel(String goalCode) {
        return Map.of(
                "Q11_1", "보습·수분감",
                "Q11_2", "트러블·여드름",
                "Q11_3", "피부 톤·미백",
                "Q11_4", "모공·피지",
                "Q11_5", "탄력·주름",
                "Q11_6", "민감한 피부 진정·장벽 강화"
        ).getOrDefault(goalCode, goalCode);
    }

    private double rate(long count, long total) {
        if (total <= 0) {
            return 0.0;
        }
        return round((double) count * 100.0 / total);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public record UserSearchCondition(
            String status,
            String provider,
            Boolean diagnosed,
            String analysisType,
            String goal,
            String keyword,
            String sort,
            int page,
            int size
    ) {

        UserSearchCondition normalize() {
            return new UserSearchCondition(
                    status,
                    provider,
                    diagnosed,
                    analysisType,
                    goal,
                    keyword,
                    sort,
                    Math.max(page, 0),
                    size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE)
            );
        }
    }
}
