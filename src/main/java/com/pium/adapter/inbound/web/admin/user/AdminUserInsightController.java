package com.pium.adapter.inbound.web.admin.user;

import com.pium.adapter.inbound.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserInsightController {

    private final AdminUserInsightQueryService queryService;

    @GetMapping("/insights/summary")
    public ApiResponse<AdminUserInsightResponse.Summary> summary() {
        return ApiResponse.ok(queryService.summary());
    }

    @GetMapping
    public ApiResponse<AdminUserInsightResponse.UserList> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) Boolean diagnosed,
            @RequestParam(required = false) String analysisType,
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "LATEST_DIAGNOSIS") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        AdminUserInsightQueryService.UserSearchCondition condition =
                new AdminUserInsightQueryService.UserSearchCondition(
                        status,
                        provider,
                        diagnosed,
                        analysisType,
                        goal,
                        keyword,
                        sort,
                        page,
                        size
                );
        return ApiResponse.ok(queryService.list(condition));
    }

    @GetMapping("/{userId}")
    public ApiResponse<AdminUserInsightResponse.UserDetail> get(
            @PathVariable String userId
    ) {
        return ApiResponse.ok(queryService.get(userId));
    }

    @GetMapping("/{userId}/skin-analysis-results")
    public ApiResponse<AdminUserInsightResponse.SkinAnalysisResultList> skinAnalysisResults(
            @PathVariable String userId,
            @RequestParam(required = false) String analysisType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(queryService.skinAnalysisResults(userId, analysisType, page, size));
    }

    @GetMapping("/insights/goals")
    public ApiResponse<AdminUserInsightResponse.GoalInsight> goals(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String analysisType
    ) {
        return ApiResponse.ok(queryService.goals(from, to, analysisType));
    }

    @GetMapping("/insights/skin-metrics")
    public ApiResponse<AdminUserInsightResponse.SkinMetricInsight> skinMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String analysisType
    ) {
        return ApiResponse.ok(queryService.skinMetrics(from, to, analysisType));
    }
}
