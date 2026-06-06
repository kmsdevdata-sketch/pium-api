package com.pium.adapter.inbound.web.admin.user;

import com.pium.adapter.outbound.auth.jwt.JwtProperties;
import com.pium.application.auth.fixture.AuthFixture;
import com.pium.application.auth.required.LoadAuthenticatedUserPort;
import com.pium.config.security.SecurityConfig;
import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserInsightController.class)
@Import(SecurityConfig.class)
class AdminUserInsightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserInsightQueryService queryService;

    @MockitoBean
    private JwtProperties jwtProperties;

    @MockitoBean
    private LoadAuthenticatedUserPort loadAuthenticatedUserPort;

    @Test
    void summary_유저인사이트요약을_조회한다() throws Exception {
        given(queryService.summary()).willReturn(summary());

        mockMvc.perform(get("/api/v1/admin/users/insights/summary")
                        .with(user(AuthFixture.createAdminAuthenticatedUser(UserId.of("admin-user"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUserCount").value(3))
                .andExpect(jsonPath("$.data.analysisTypeCounts[0].analysisType").value("SURVEY"))
                .andExpect(jsonPath("$.data.topGoals[0].goalCode").value("Q11_1"));
    }

    @Test
    void list_유저목록을_조회한다() throws Exception {
        given(queryService.list(any())).willReturn(userList());

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("status", "ACTIVE")
                        .param("diagnosed", "true")
                        .with(user(AuthFixture.createAdminAuthenticatedUser(UserId.of("admin-user"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.users[0].userId").value("user-001"))
                .andExpect(jsonPath("$.data.users[0].latestAnalysisType").value("SURVEY"));
    }

    @Test
    void get_유저상세를_조회한다() throws Exception {
        given(queryService.get("user-001")).willReturn(userDetail());

        mockMvc.perform(get("/api/v1/admin/users/user-001")
                        .with(user(AuthFixture.createAdminAuthenticatedUser(UserId.of("admin-user"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("user-001"))
                .andExpect(jsonPath("$.data.latestDiagnosisSummary.resultId").value("result-001"));
    }

    @Test
    void skinAnalysisResults_유저진단이력을_조회한다() throws Exception {
        given(queryService.skinAnalysisResults("user-001", null, 0, 20))
                .willReturn(new AdminUserInsightResponse.SkinAnalysisResultList(
                        1,
                        0,
                        20,
                        List.of(diagnosisSummary())
                ));

        mockMvc.perform(get("/api/v1/admin/users/user-001/skin-analysis-results")
                        .with(user(AuthFixture.createAdminAuthenticatedUser(UserId.of("admin-user"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.results[0].resultId").value("result-001"))
                .andExpect(jsonPath("$.data.results[0].skinMetricScores[0].metric").value("DRYNESS"));
    }

    @Test
    void goals_goal분포를_조회한다() throws Exception {
        given(queryService.goals(null, null, null)).willReturn(new AdminUserInsightResponse.GoalInsight(
                List.of(goalCount("Q11_1", 2, 66.7))
        ));

        mockMvc.perform(get("/api/v1/admin/users/insights/goals")
                        .with(user(AuthFixture.createAdminAuthenticatedUser(UserId.of("admin-user"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.goals[0].goalCode").value("Q11_1"))
                .andExpect(jsonPath("$.data.goals[0].rate").value(66.7));
    }

    @Test
    void skinMetrics_피부지표분포를_조회한다() throws Exception {
        given(queryService.skinMetrics(null, null, null)).willReturn(new AdminUserInsightResponse.SkinMetricInsight(
                List.of(new AdminUserInsightResponse.SkinMetricDistribution(
                        "DRYNESS",
                        72.0,
                        0,
                        1,
                        1,
                        0.0,
                        50.0,
                        50.0
                ))
        ));

        mockMvc.perform(get("/api/v1/admin/users/insights/skin-metrics")
                        .with(user(AuthFixture.createAdminAuthenticatedUser(UserId.of("admin-user"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metrics[0].metric").value("DRYNESS"))
                .andExpect(jsonPath("$.data.metrics[0].averageScore").value(72.0));
    }

    @Test
    void list_일반사용자는_조회할수없다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user(AuthFixture.createAuthenticatedUser(UserId.of("user-001"))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    private AdminUserInsightResponse.Summary summary() {
        return new AdminUserInsightResponse.Summary(
                3,
                2,
                1,
                0,
                1,
                2,
                4,
                2,
                1,
                2,
                3,
                List.of(new AdminUserInsightResponse.AnalysisTypeCount("SURVEY", 3)),
                List.of(goalCount("Q11_1", 2, 50.0))
        );
    }

    private AdminUserInsightResponse.UserList userList() {
        return new AdminUserInsightResponse.UserList(
                1,
                0,
                20,
                List.of(new AdminUserInsightResponse.UserItem(
                        "user-001",
                        "피움",
                        null,
                        "GOOGLE",
                        "ACTIVE",
                        "USER",
                        LocalDateTime.of(2026, 6, 1, 12, 0),
                        LocalDateTime.of(2026, 6, 2, 12, 0),
                        1,
                        LocalDateTime.of(2026, 6, 3, 12, 0),
                        "SURVEY",
                        List.of("Q11_1")
                ))
        );
    }

    private AdminUserInsightResponse.UserDetail userDetail() {
        return new AdminUserInsightResponse.UserDetail(
                "user-001",
                "피움",
                null,
                "GOOGLE",
                "go***01",
                "ACTIVE",
                "USER",
                LocalDateTime.of(2026, 6, 1, 12, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                LocalDateTime.of(2026, 6, 2, 12, 0),
                1,
                diagnosisSummary(),
                List.of(goalCount("Q11_1", 1, 100.0))
        );
    }

    private AdminUserInsightResponse.DiagnosisSummary diagnosisSummary() {
        return new AdminUserInsightResponse.DiagnosisSummary(
                "result-001",
                "SURVEY",
                LocalDateTime.of(2026, 6, 3, 12, 0),
                List.of("Q11_1"),
                List.of(new AdminUserInsightResponse.SkinMetricScore("DRYNESS", 72))
        );
    }

    private AdminUserInsightResponse.GoalCount goalCount(String goalCode, long count, double rate) {
        return new AdminUserInsightResponse.GoalCount(goalCode, "보습·수분감", count, rate);
    }
}
