package com.pium.application.auth.required;

import com.pium.domain.user.model.UserProfile;

/**
 * Required Port
 * - UserProfile 을 영속화하기 위한 저장 포트
 */
public interface SaveUserProfilePort {

    /** UserProfile 저장 */
    void save(UserProfile userProfile);
}
