package com.pium.application.skinanalysis.image.dto;

import com.pium.domain.user.vo.UserId;

public record PreAnalyzeImageCommand(
        UserId userId,
        SkinImageFile image
) {
}
