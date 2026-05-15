package com.pium.application.auth.required;

import com.pium.domain.user.vo.UserId;

public interface IssueAccessTokenPort {

    String issue(UserId userId);
}
