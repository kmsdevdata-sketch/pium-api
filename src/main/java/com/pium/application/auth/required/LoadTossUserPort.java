package com.pium.application.auth.required;

import com.pium.application.auth.required.dto.TossAuthenticatedUser;

public interface LoadTossUserPort {

    TossAuthenticatedUser load(String accessToken);
}
