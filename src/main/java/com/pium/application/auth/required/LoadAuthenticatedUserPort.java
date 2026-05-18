package com.pium.application.auth.required;

import com.pium.adapter.inbound.web.auth.AuthenticatedUser;
import com.pium.domain.user.vo.UserId;

import java.util.Optional;

public interface LoadAuthenticatedUserPort {

    Optional<AuthenticatedUser> load(UserId userId);
}
