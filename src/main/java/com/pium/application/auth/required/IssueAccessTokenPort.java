package com.pium.application.auth.required;

import com.pium.domain.user.vo.UserId;

/**
 * Required Port
 * - 인증된 사용자를 위한 서비스 엑세스 토큰을 발급하는 역할
 */
public interface IssueAccessTokenPort {

    String issue(UserId userId);
}
