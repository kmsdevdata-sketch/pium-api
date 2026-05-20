package com.pium.application.auth.required;

import com.pium.domain.user.model.UserProfile;
import com.pium.domain.user.vo.UserId;

import java.util.Optional;

public interface LoadUserProfilePort {

    Optional<UserProfile> findByUserId(UserId userId);
}
