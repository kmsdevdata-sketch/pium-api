package com.pium.adapter.outbound.productprofile.openai;

import java.util.List;

/**
 * OpenAI Responses API 응답.
 */
record OpenAiResponsesApiResponse(
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
