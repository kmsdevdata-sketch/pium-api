package com.pium.domain.user.fixture;

import com.pium.domain.user.model.User;

public class UserFixture {

    public static User createUser() {
        return User.create();
    }
}
