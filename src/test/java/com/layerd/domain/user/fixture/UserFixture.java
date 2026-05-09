package com.layerd.domain.user.fixture;

import com.layerd.domain.user.model.User;

public class UserFixture {

    public static User createUser() {
        return User.create();
    }
}
