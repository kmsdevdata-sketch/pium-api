package com.pium.adapter.inbound.web.user;

import com.pium.application.user.bootstrap.dto.UserBootstrapView;

public record UserBootstrapResponse(
        boolean hasDiagnosis,
        String entryPoint
) {

    public static UserBootstrapResponse from(UserBootstrapView view) {
        return new UserBootstrapResponse(
                view.hasDiagnosis(),
                view.entryPoint().name()
        );
    }
}
