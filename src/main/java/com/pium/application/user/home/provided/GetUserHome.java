package com.pium.application.user.home.provided;

import com.pium.application.user.home.dto.UserHomeView;
import com.pium.domain.user.vo.UserId;

/**
 * 홈 화면에 필요한 사용자 요약 정보를 조회한다.
 */
public interface GetUserHome {

    UserHomeView getUserHome(UserId userId);
}
