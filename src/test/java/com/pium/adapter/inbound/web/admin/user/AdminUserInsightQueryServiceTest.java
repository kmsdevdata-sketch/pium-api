package com.pium.adapter.inbound.web.admin.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest(properties = {
        "spring.flyway.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:admin-user-insight;MODE=MySQL;NON_KEYWORDS=USERS;DB_CLOSE_DELAY=-1"
})
@Import(AdminUserInsightQueryService.class)
class AdminUserInsightQueryServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AdminUserInsightQueryService queryService;

    @BeforeEach
    void setUp() {
        createTables();
        clearTables();
        insertData();
    }

    @Test
    void summary_유저와_진단_요약을_집계한다() {
        AdminUserInsightResponse.Summary summary = queryService.summary();

        assertThat(summary.totalUserCount()).isEqualTo(2);
        assertThat(summary.activeUserCount()).isEqualTo(1);
        assertThat(summary.diagnosedUserCount()).isEqualTo(1);
        assertThat(summary.undiagnosedUserCount()).isEqualTo(1);
        assertThat(summary.totalDiagnosisCount()).isEqualTo(2);
        assertThat(summary.analysisTypeCounts()).extracting("analysisType")
                .containsExactlyInAnyOrder("SURVEY", "IMAGE");
        assertThat(summary.topGoals().get(0).goalCode()).isEqualTo("Q11_1");
    }

    @Test
    void list_유저목록에_진단요약을_포함한다() {
        AdminUserInsightResponse.UserList result = queryService.list(
                new AdminUserInsightQueryService.UserSearchCondition(
                        "ACTIVE",
                        null,
                        true,
                        null,
                        null,
                        null,
                        "DIAGNOSIS_COUNT",
                        0,
                        20
                )
        );

        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.users().get(0).userId()).isEqualTo("user-001");
        assertThat(result.users().get(0).diagnosisCount()).isEqualTo(2);
        assertThat(result.users().get(0).latestAnalysisType()).isEqualTo("IMAGE");
        assertThat(result.users().get(0).goals()).contains("Q11_1", "Q11_2");
    }

    @Test
    void get_유저상세에_최신진단과_goal요약을_포함한다() {
        AdminUserInsightResponse.UserDetail result = queryService.get("user-001");

        assertThat(result.userId()).isEqualTo("user-001");
        assertThat(result.providerUserIdMasked()).isEqualTo("go***01");
        assertThat(result.latestDiagnosisSummary().resultId()).isEqualTo("result-002");
        assertThat(result.latestDiagnosisSummary().skinMetricScores()).hasSize(2);
        assertThat(result.goalSummary()).extracting("goalCode").contains("Q11_1", "Q11_2");
    }

    @Test
    void goals_goal분포를_집계한다() {
        AdminUserInsightResponse.GoalInsight result = queryService.goals(null, null, null);

        assertThat(result.goals()).hasSize(2);
        assertThat(result.goals().get(0).goalCode()).isEqualTo("Q11_1");
        assertThat(result.goals().get(0).count()).isEqualTo(2);
    }

    @Test
    void skinMetrics_피부지표분포를_집계한다() {
        AdminUserInsightResponse.SkinMetricInsight result = queryService.skinMetrics(null, null, null);

        assertThat(result.metrics()).extracting("metric").contains("DRYNESS", "BARRIER");
        AdminUserInsightResponse.SkinMetricDistribution dryness = result.metrics().stream()
                .filter(metric -> metric.metric().equals("DRYNESS"))
                .findFirst()
                .orElseThrow();
        assertThat(dryness.averageScore()).isEqualTo(70.0);
        assertThat(dryness.highCount()).isEqualTo(1);
        assertThat(dryness.midCount()).isEqualTo(1);
    }

    private void createTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id VARCHAR(64) PRIMARY KEY,
                    role VARCHAR(32) NOT NULL,
                    status VARCHAR(32) NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_profile (
                    user_profile_id VARCHAR(64) PRIMARY KEY,
                    user_id VARCHAR(64) NOT NULL,
                    nickname VARCHAR(100) NOT NULL,
                    profile_image_url VARCHAR(512),
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_oauth (
                    user_oauth_id VARCHAR(64) PRIMARY KEY,
                    user_id VARCHAR(64) NOT NULL,
                    provider VARCHAR(32) NOT NULL,
                    provider_user_id VARCHAR(128) NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL,
                    last_login_at TIMESTAMP
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS skin_analysis_result (
                    result_id VARCHAR(64) PRIMARY KEY,
                    user_id VARCHAR(64) NOT NULL,
                    analysis_type VARCHAR(32) NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS skin_analysis_goal (
                    result_id VARCHAR(64) NOT NULL,
                    goal_code VARCHAR(64) NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS skin_metric_score (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    result_id VARCHAR(64) NOT NULL,
                    metric VARCHAR(64) NOT NULL,
                    score_value INT NOT NULL
                )
                """);
    }

    private void clearTables() {
        jdbcTemplate.update("DELETE FROM skin_metric_score");
        jdbcTemplate.update("DELETE FROM skin_analysis_goal");
        jdbcTemplate.update("DELETE FROM skin_analysis_result");
        jdbcTemplate.update("DELETE FROM user_oauth");
        jdbcTemplate.update("DELETE FROM user_profile");
        jdbcTemplate.update("DELETE FROM users");
    }

    private void insertData() {
        jdbcTemplate.update("""
                INSERT INTO users(user_id, role, status, created_at, updated_at)
                VALUES
                ('user-001', 'USER', 'ACTIVE', '2026-06-01 10:00:00', '2026-06-01 10:00:00'),
                ('user-002', 'USER', 'WITHDRAWN', '2026-06-02 10:00:00', '2026-06-02 10:00:00')
                """);
        jdbcTemplate.update("""
                INSERT INTO user_profile(user_profile_id, user_id, nickname, profile_image_url, created_at, updated_at)
                VALUES
                ('profile-001', 'user-001', '피움', NULL, '2026-06-01 10:00:00', '2026-06-01 10:00:00'),
                ('profile-002', 'user-002', '민트', NULL, '2026-06-02 10:00:00', '2026-06-02 10:00:00')
                """);
        jdbcTemplate.update("""
                INSERT INTO user_oauth(user_oauth_id, user_id, provider, provider_user_id, created_at, updated_at, last_login_at)
                VALUES
                ('oauth-001', 'user-001', 'GOOGLE', 'google-user-001', '2026-06-01 10:00:00', '2026-06-01 10:00:00', '2026-06-03 10:00:00')
                """);
        jdbcTemplate.update("""
                INSERT INTO skin_analysis_result(result_id, user_id, analysis_type, created_at, updated_at)
                VALUES
                ('result-001', 'user-001', 'SURVEY', '2026-06-04 10:00:00', '2026-06-04 10:00:00'),
                ('result-002', 'user-001', 'IMAGE', '2026-06-05 10:00:00', '2026-06-05 10:00:00')
                """);
        jdbcTemplate.update("""
                INSERT INTO skin_analysis_goal(result_id, goal_code)
                VALUES
                ('result-001', 'Q11_1'),
                ('result-002', 'Q11_1'),
                ('result-002', 'Q11_2')
                """);
        jdbcTemplate.update("""
                INSERT INTO skin_metric_score(result_id, metric, score_value)
                VALUES
                ('result-001', 'DRYNESS', 65),
                ('result-001', 'BARRIER', 55),
                ('result-002', 'DRYNESS', 75),
                ('result-002', 'BARRIER', 72)
                """);
    }
}
