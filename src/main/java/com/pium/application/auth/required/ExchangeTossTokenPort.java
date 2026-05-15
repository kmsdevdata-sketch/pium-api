package com.pium.application.auth.required;

import com.pium.application.auth.required.dto.TossAccessToken;

public interface ExchangeTossTokenPort {

    TossAccessToken exchange(String authorizationCode, String referrer);
}
