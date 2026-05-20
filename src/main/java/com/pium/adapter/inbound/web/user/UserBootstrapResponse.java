package com.pium.adapter.inbound.web.user;

import com.pium.application.user.bootstrap.dto.UserBootstrapView;

public record UserBootstrapResponse(
        String userName,
        boolean hasDiagnosis,
        String entryPoint
) {

    public static UserBootstrapResponse from(UserBootstrapView view) {
        return new UserBootstrapResponse(
                view.userName(),
                view.hasDiagnosis(),
                view.entryPoint().name()
        );
    }
}
