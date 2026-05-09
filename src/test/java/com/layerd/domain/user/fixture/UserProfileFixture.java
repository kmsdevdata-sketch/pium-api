package com.layerd.domain.user.fixture;

import com.layerd.domain.user.model.User;
import com.layerd.domain.user.model.UserProfile;

public class UserProfileFixture {

    public static UserProfile createUserProfile(User user) {
        return UserProfile.create(
                user.getId(),
                "testNickName",
                "https://test.com/test.png"

        );
    }
}
