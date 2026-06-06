package com.pium.adapter.outbound.skinanalysis.image.openai;

import java.util.List;

record OpenAiSkinImageAnalysisResponse(
        String status,
        List<Output> output
) {

    record Output(
            String type,
            List<Content> content
    ) {
    }

    record Content(
            String type,
            String text,
            String refusal
    ) {
    }
}
