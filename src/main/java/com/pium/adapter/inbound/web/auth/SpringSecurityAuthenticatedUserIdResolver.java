package com.pium.adapter.inbound.web.auth;

import com.pium.application.skinanalysis.exception.SurveyApplicationErrorCode;
import com.pium.application.skinanalysis.exception.SurveyApplicationException;
import com.pium.domain.user.vo.UserId;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityAuthenticatedUserIdResolver implements AuthenticatedUserIdResolver {

    @Override
    public UserId resolve(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SurveyApplicationException(SurveyApplicationErrorCode.CURRENT_USER_UNAVAILABLE);
        }

        UserId userId = resolveCandidate(authentication.getPrincipal());
        if (userId != null) {
            return userId;
        }

        userId = resolveCandidate(authentication.getDetails());
        if (userId != null) {
            return userId;
        }

        String name = authentication.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) {
            throw new SurveyApplicationException(SurveyApplicationErrorCode.CURRENT_USER_UNAVAILABLE);
        }

        return UserId.of(name.trim());
    }

    private UserId resolveCandidate(Object candidate) {
        if (candidate instanceof UserIdPrincipal principal) {
            return UserId.of(principal.userId());
        }
        return null;
    }
}
