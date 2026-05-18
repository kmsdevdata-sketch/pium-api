package com.pium.application.auth.required;

import com.pium.domain.user.model.User;

/**
 * Required Port
 * - User 를 영속화하기 위한 저장 포트
 */
public interface SaveUserPort {

    /** 유저 저장*/
    void save(User user);
}
