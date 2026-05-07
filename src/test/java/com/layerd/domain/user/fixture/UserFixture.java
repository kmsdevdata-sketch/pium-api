package com.layerd.domain.user.fixture;

import com.layerd.domain.user.User;

public class UserFixture {

    public static User createUser() {
        return User.create();
    }
}
