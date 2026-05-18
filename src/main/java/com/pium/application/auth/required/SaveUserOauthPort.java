package com.pium.application.auth.required;

import com.pium.domain.user.model.UserOauth;

/**
 * Required Port
 * - UserOauth 를 영속화하기 위한 저장 포트
 */
public interface SaveUserOauthPort {

    /** UserOauth 저장 */
    void save(UserOauth userOauth);
}
