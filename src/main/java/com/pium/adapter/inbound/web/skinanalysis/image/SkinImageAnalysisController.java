package com.pium.adapter.inbound.web.skinanalysis.image;

import com.pium.adapter.inbound.response.ApiResponse;
import com.pium.adapter.inbound.web.auth.AuthenticatedUserIdResolver;
import com.pium.application.skinanalysis.image.dto.AnalyzeImageResultView;
import com.pium.application.skinanalysis.image.dto.PreAnalyzeImageView;
import com.pium.application.skinanalysis.image.provided.AnalyzeSkinImage;
import com.pium.application.skinanalysis.image.provided.PreAnalyzeSkinImage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/skin-images")
public class SkinImageAnalysisController {

    private final PreAnalyzeSkinImage preAnalyzeSkinImage;
    private final AnalyzeSkinImage analyzeSkinImage;
    private final AuthenticatedUserIdResolver authenticatedUserIdResolver;

    @PostMapping(
            value = "/pre-analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<PreAnalyzeSkinImageResponse> preAnalyze(
            @RequestPart("image") MultipartFile image,
            Authentication authentication
    ) {
        PreAnalyzeImageView response = preAnalyzeSkinImage.preAnalyze(
                PreAnalyzeSkinImageRequest.toCommand(
                        authenticatedUserIdResolver.resolve(authentication),
                        image
                )
        );

        return ApiResponse.ok(PreAnalyzeSkinImageResponse.from(response));
    }

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<AnalyzeSkinImageResponse>> analyze(
            @Valid @RequestBody AnalyzeSkinImageRequest request,
            Authentication authentication
    ) {
        AnalyzeImageResultView response = analyzeSkinImage.analyze(
                request.toCommand(authenticatedUserIdResolver.resolve(authentication))
        );
        HttpStatus status = response.status() == AnalyzeImageResultView.Status.PROCESSING
                ? HttpStatus.ACCEPTED
                : HttpStatus.OK;

        return ResponseEntity
                .status(status)
                .body(ApiResponse.ok(AnalyzeSkinImageResponse.from(response)));
    }
}
