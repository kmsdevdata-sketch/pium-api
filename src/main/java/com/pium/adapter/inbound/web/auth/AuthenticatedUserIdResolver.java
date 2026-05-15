package com.pium.adapter.inbound.web.auth;

import com.pium.domain.user.vo.UserId;
import org.springframework.security.core.Authentication;

public interface AuthenticatedUserIdResolver {

    UserId resolve(Authentication authentication);
}
