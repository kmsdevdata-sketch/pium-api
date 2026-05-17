package com.pium.application.user.bootstrap.provided;

import com.pium.application.user.bootstrap.dto.UserBootstrapView;
import com.pium.domain.user.vo.UserId;

/**
 * 현재 로그인 사용자의 초기 진입 상태를 조회한다.
 */
public interface GetUserBootstrap {

    UserBootstrapView getUserBootstrap(UserId userId);
}
