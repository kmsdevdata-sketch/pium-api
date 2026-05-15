package com.pium.adapter.inbound.web.auth;

import com.pium.domain.user.vo.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpringSecurityAuthenticatedUserIdResolverTest {

    private final SpringSecurityAuthenticatedUserIdResolver resolver = new SpringSecurityAuthenticatedUserIdResolver();

    @Test
    void resolve_사용자계약을_구현한_principal에서_userId를_반환한다() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(new TestPrincipal("user-001"), null, List.of());

        UserId userId = resolver.resolve(authentication);

        assertThat(userId).isEqualTo(UserId.of("user-001"));
    }

    @Test
    void resolve_계약이_없으면_authentication_name을_fallback으로_사용한다() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("user-002", null, List.of());

        UserId userId = resolver.resolve(authentication);

        assertThat(userId).isEqualTo(UserId.of("user-002"));
    }

    private record TestPrincipal(String userId) implements UserIdPrincipal {
    }
}
