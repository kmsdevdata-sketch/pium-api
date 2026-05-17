package com.pium.adapter.inbound.web.user;

import com.pium.adapter.inbound.response.ApiResponse;
import com.pium.adapter.inbound.web.auth.AuthenticatedUser;
import com.pium.application.skinanalysis.result.dto.SkinAnalysisResultView;
import com.pium.application.skinanalysis.result.provided.GetSkinAnalysisResult;
import com.pium.application.user.bootstrap.dto.UserBootstrapView;
import com.pium.application.user.bootstrap.provided.GetUserBootstrap;
import com.pium.domain.skinanalysis.vo.SkinAnalysisResultId;
import com.pium.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final GetUserBootstrap getUserBootstrap;
    private final GetSkinAnalysisResult getSkinAnalysisResultUseCase;

    /**
     * 현재 로그인 사용자의 초기 진입 상태 조회 API
     */
    @GetMapping("/me/bootstrap")
    public ApiResponse<UserBootstrapResponse> getUserBootstrap(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        UserBootstrapView response = getUserBootstrap.getUserBootstrap(UserId.of(user.userId()));
        return ApiResponse.ok(UserBootstrapResponse.from(response));
    }

    /**
     * 최신 피부 분석 결과 조회 API
     */
    @GetMapping("/me/skin-analysis-results/latest")
    public ApiResponse<SkinAnalysisResultResponse> getLatestSkinAnalysisResult(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        SkinAnalysisResultView response = getSkinAnalysisResultUseCase.getLatest(UserId.of(user.userId()));
        return ApiResponse.ok(SkinAnalysisResultResponse.from(response));
    }

    /**
     * 특정 피부 분석 결과 조회 API
     */
    @GetMapping("/me/skin-analysis-results/{resultId}")
    public ApiResponse<SkinAnalysisResultResponse> getSkinAnalysisResult(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String resultId
    ) {
        SkinAnalysisResultView response = getSkinAnalysisResultUseCase.get(
                UserId.of(user.userId()),
                SkinAnalysisResultId.of(resultId)
        );
        return ApiResponse.ok(SkinAnalysisResultResponse.from(response));
    }
}
