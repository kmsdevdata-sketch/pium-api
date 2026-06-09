package com.pium.application.user.withdrawn.provided;

import com.pium.domain.user.vo.UserId;

/**
 * 사용자 탈퇴
 */
public interface WithdrawnUser {

    void withdrawn(UserId userId);
}
