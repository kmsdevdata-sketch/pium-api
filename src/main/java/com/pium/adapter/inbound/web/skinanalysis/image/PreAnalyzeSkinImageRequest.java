package com.pium.adapter.inbound.web.skinanalysis.image;

import com.pium.adapter.outbound.skinanalysis.exception.SkinAnalysisAdapterErrorCode;
import com.pium.adapter.outbound.skinanalysis.exception.SkinAnalysisAdapterException;
import com.pium.application.skinanalysis.image.dto.PreAnalyzeImageCommand;
import com.pium.application.skinanalysis.image.dto.SkinImageFile;
import com.pium.domain.user.vo.UserId;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 사진 선분석 요청 DTO
 */
public final class PreAnalyzeSkinImageRequest {

    private PreAnalyzeSkinImageRequest() {
    }

    public static PreAnalyzeImageCommand toCommand(UserId userId, MultipartFile image) {
        return new PreAnalyzeImageCommand(userId, imageFile(image));
    }

    private static SkinImageFile imageFile(MultipartFile image) {
        if (image == null) {
            throw new SkinAnalysisAdapterException(SkinAnalysisAdapterErrorCode.INVALID_IMAGE_PAYLOAD);
        }

        try {
            return new SkinImageFile(
                    image.getOriginalFilename(),
                    image.getContentType(),
                    image.getSize(),
                    image.getBytes()
            );
        } catch (IOException e) {
            throw new SkinAnalysisAdapterException(SkinAnalysisAdapterErrorCode.INVALID_IMAGE_PAYLOAD);
        }
    }
}
